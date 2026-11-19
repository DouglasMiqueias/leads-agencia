package br.com.agencia.leads.web;

import br.com.agencia.leads.model.Lead;
import br.com.agencia.leads.model.Lead.Etapa;
import br.com.agencia.leads.model.LeadForm;
import br.com.agencia.leads.model.UserAccount;
import br.com.agencia.leads.repo.LeadRepository;
import br.com.agencia.leads.repo.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class LeadController {
    private final LeadRepository leads;
    private final UserRepository users;

    public LeadController(LeadRepository leads, UserRepository users) {
        this.leads = leads;
        this.users = users;
    }

    @GetMapping("/")
    public String board(@RequestParam(required = false) String q,
                        @RequestParam(required = false) Lead.Prioridade prioridade,
                        @RequestParam(required = false) Lead.Origem origem,
                        @RequestParam(required = false) Long responsavel,
                        Model m) {
        List<Lead> todos = leads.findAll();
        String busca = q == null ? "" : q.toLowerCase().trim();
        List<Lead> vis = todos.stream()
            .filter(l -> busca.isEmpty() || (nz(l.getNome()) + " " + nz(l.getEmpresa())).toLowerCase().contains(busca))
            .filter(l -> prioridade == null || l.getPrioridade() == prioridade)
            .filter(l -> origem == null || l.getOrigem() == origem)
            .filter(l -> responsavel == null || (l.getResponsavel() != null && l.getResponsavel().getId().equals(responsavel)))
            .toList();

        Map<Etapa, List<Lead>> colunas = new LinkedHashMap<>();
        Map<Etapa, BigDecimal> totais = new LinkedHashMap<>();
        for (Etapa e : Etapa.values()) {
            List<Lead> ls = vis.stream().filter(l -> l.getEtapa() == e)
                .sorted(Comparator.comparingInt(Lead::getPontuacao).reversed()).toList();
            colunas.put(e, ls);
            totais.put(e, ls.stream().map(l -> l.getOrcamento() == null ? BigDecimal.ZERO : l.getOrcamento())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        List<Lead> abertos = todos.stream().filter(l -> l.getEtapa().isAberta()).toList();
        long ganhos = todos.stream().filter(l -> l.getEtapa() == Etapa.GANHO).count();
        long perdidos = todos.stream().filter(l -> l.getEtapa() == Etapa.PERDIDO).count();

        m.addAttribute("colunas", colunas);
        m.addAttribute("totais", totais);
        m.addAttribute("total", todos.size());
        m.addAttribute("valorAberto", abertos.stream().map(l -> l.getOrcamento() == null ? BigDecimal.ZERO : l.getOrcamento())
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        m.addAttribute("conversao", ganhos + perdidos == 0 ? 0 : Math.round(100.0 * ganhos / (ganhos + perdidos)));
        m.addAttribute("parados", abertos.stream().filter(l -> l.getDiasSemContato() >= 7).count());
        m.addAttribute("q", q);
        m.addAttribute("fPrioridade", prioridade);
        m.addAttribute("fOrigem", origem);
        m.addAttribute("fResponsavel", responsavel);
        refs(m);
        return "board";
    }

    @GetMapping("/leads/novo")
    public String novo(Model m, Principal p) {
        LeadForm f = new LeadForm();
        users.findByEmailIgnoreCase(p.getName()).ifPresent(u -> f.setResponsavelId(u.getId()));
        m.addAttribute("form", f);
        refs(m);
        return "lead-form";
    }

    @GetMapping("/leads/{id}")
    public String editar(@PathVariable Long id, Model m) {
        m.addAttribute("form", LeadForm.from(leads.findById(id).orElseThrow()));
        refs(m);
        return "lead-form";
    }

    @PostMapping("/leads")
    public String salvar(@Valid @ModelAttribute("form") LeadForm form, BindingResult br, Model m) {
        if (br.hasErrors()) {
            refs(m);
            return "lead-form";
        }
        Lead l = form.getId() == null ? new Lead() : leads.findById(form.getId()).orElseThrow();
        form.apply(l);
        l.setResponsavel(form.getResponsavelId() == null ? null : users.findById(form.getResponsavelId()).orElse(null));
        leads.save(l);
        return "redirect:/";
    }

    @PostMapping("/leads/{id}/excluir")
    public String excluir(@PathVariable Long id) {
        leads.deleteById(id);
        return "redirect:/";
    }

    @PostMapping("/leads/{id}/avancar")
    public String avancar(@PathVariable Long id) {
        Lead l = leads.findById(id).orElseThrow();
        if (l.getEtapa().ordinal() < Etapa.GANHO.ordinal()) {
            l.setEtapa(Etapa.values()[l.getEtapa().ordinal() + 1]);
            l.setUltimoContato(LocalDate.now());
            leads.save(l);
        }
        return "redirect:/";
    }

    /** Chamado pelo arrastar e soltar do quadro. */
    @PostMapping("/leads/{id}/mover")
    @ResponseBody
    public ResponseEntity<Void> mover(@PathVariable Long id, @RequestParam Etapa etapa) {
        Lead l = leads.findById(id).orElseThrow();
        l.setEtapa(etapa);
        l.setUltimoContato(LocalDate.now());
        leads.save(l);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/leads/exportar.csv")
    public ResponseEntity<byte[]> csv() {
        StringBuilder sb = new StringBuilder("\uFEFFNome;Empresa;E-mail;Telefone;Origem;Tipo;Orçamento;Etapa;Prioridade;Pontuação;Responsável;Último contato;Motivo da perda\n");
        for (Lead l : leads.findAll()) {
            sb.append(String.join(";", List.of(
                c(l.getNome()), c(l.getEmpresa()), c(l.getEmail()), c(l.getTelefone()),
                c(l.getOrigem().getLabel()), c(l.getTipo().getLabel()), c(String.valueOf(l.getOrcamento())),
                c(l.getEtapa().getLabel()), c(l.getPrioridade().getLabel()), c(String.valueOf(l.getPontuacao())),
                c(l.getResponsavel() == null ? "" : l.getResponsavel().getNome()),
                c(String.valueOf(l.getUltimoContato())), c(l.getMotivoPerda())))).append('\n');
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leads.csv")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void refs(Model m) {
        m.addAttribute("etapas", Etapa.values());
        m.addAttribute("prioridades", Lead.Prioridade.values());
        m.addAttribute("origens", Lead.Origem.values());
        m.addAttribute("tipos", Lead.Tipo.values());
        m.addAttribute("prazos", Lead.Prazo.values());
        m.addAttribute("usuarios", users.findByAtivoTrueOrderByNome());
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static String c(String s) { return "\"" + nz(s).replace("\"", "\"\"") + "\""; }
}
