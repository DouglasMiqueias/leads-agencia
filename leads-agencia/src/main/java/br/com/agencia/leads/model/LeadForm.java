package br.com.agencia.leads.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LeadForm {
    private Long id;
    @NotBlank(message = "Informe o nome") private String nome;
    private String empresa;
    private String email;
    private String telefone;
    private Lead.Origem origem = Lead.Origem.INDICACAO;
    private Lead.Tipo tipo = Lead.Tipo.SITE_INSTITUCIONAL;
    private BigDecimal orcamento;
    private Lead.Prazo prazo = Lead.Prazo.ATE_90;
    private Lead.Etapa etapa = Lead.Etapa.NOVO;
    private Lead.Prioridade prioridadeManual;
    private boolean decisor;
    private boolean respondeu;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate ultimoContato = LocalDate.now();
    private String notas;
    private String motivoPerda;
    private Long responsavelId;

    public static LeadForm from(Lead l) {
        LeadForm f = new LeadForm();
        f.id = l.getId(); f.nome = l.getNome(); f.empresa = l.getEmpresa(); f.email = l.getEmail();
        f.telefone = l.getTelefone(); f.origem = l.getOrigem(); f.tipo = l.getTipo(); f.orcamento = l.getOrcamento();
        f.prazo = l.getPrazo(); f.etapa = l.getEtapa(); f.prioridadeManual = l.getPrioridadeManual();
        f.decisor = l.isDecisor(); f.respondeu = l.isRespondeu(); f.ultimoContato = l.getUltimoContato();
        f.notas = l.getNotas(); f.motivoPerda = l.getMotivoPerda();
        f.responsavelId = l.getResponsavel() == null ? null : l.getResponsavel().getId();
        return f;
    }

    public void apply(Lead l) {
        l.setNome(nome.trim()); l.setEmpresa(empresa); l.setEmail(email); l.setTelefone(telefone);
        l.setOrigem(origem); l.setTipo(tipo); l.setOrcamento(orcamento == null ? BigDecimal.ZERO : orcamento);
        l.setPrazo(prazo); l.setEtapa(etapa); l.setPrioridadeManual(prioridadeManual);
        l.setDecisor(decisor); l.setRespondeu(respondeu);
        l.setUltimoContato(ultimoContato == null ? LocalDate.now() : ultimoContato);
        l.setNotas(notas); l.setMotivoPerda(motivoPerda);
    }
}
