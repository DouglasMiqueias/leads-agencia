package br.com.agencia.leads.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "leads")
@Getter @Setter
public class Lead {

    @Getter @RequiredArgsConstructor
    public enum Etapa {
        NOVO("Novo"), CONTATO("Contato feito"), REUNIAO("Reunião"), PROPOSTA("Proposta enviada"),
        NEGOCIACAO("Negociação"), GANHO("Ganho"), PERDIDO("Perdido");
        private final String label;
        public boolean isAberta() { return this != GANHO && this != PERDIDO; }
    }

    @Getter @RequiredArgsConstructor
    public enum Prioridade { ALTA("Alta"), MEDIA("Média"), BAIXA("Baixa"); private final String label; }

    @Getter @RequiredArgsConstructor
    public enum Prazo { MENOS_30("Menos de 30 dias"), ATE_90("1 a 3 meses"), INDEFINIDO("Sem prazo definido"); private final String label; }

    @Getter @RequiredArgsConstructor
    public enum Origem {
        INDICACAO("Indicação"), INSTAGRAM("Instagram"), LINKEDIN("LinkedIn"), GOOGLE("Google"),
        PROSPECCAO("Prospecção ativa"), SITE("Site da agência");
        private final String label;
    }

    @Getter @RequiredArgsConstructor
    public enum Tipo {
        SITE_INSTITUCIONAL("Site institucional"), ECOMMERCE("E-commerce"), SISTEMA("Sistema web"),
        REDESIGN("Redesign"), LANDING("Landing page"), OUTRO("Outro");
        private final String label;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    private String empresa;
    private String email;
    private String telefone;
    @Enumerated(EnumType.STRING) private Origem origem = Origem.INDICACAO;
    @Enumerated(EnumType.STRING) private Tipo tipo = Tipo.SITE_INSTITUCIONAL;
    private BigDecimal orcamento = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) private Prazo prazo = Prazo.ATE_90;
    @Enumerated(EnumType.STRING) private Etapa etapa = Etapa.NOVO;
    @Enumerated(EnumType.STRING) private Prioridade prioridadeManual;
    private boolean decisor;
    private boolean respondeu;
    private LocalDate ultimoContato = LocalDate.now();
    @Column(length = 4000) private String notas;
    private String motivoPerda;
    @ManyToOne(fetch = FetchType.EAGER) private UserAccount responsavel;
    private LocalDateTime criadoEm = LocalDateTime.now();

    public long getDiasSemContato() {
        return ultimoContato == null ? 0 : ChronoUnit.DAYS.between(ultimoContato, LocalDate.now());
    }

    /** Pontuação automática: orçamento, prazo, origem, decisor, engajamento e tempo parado. */
    public int getPontuacao() {
        int s = 0;
        BigDecimal o = orcamento == null ? BigDecimal.ZERO : orcamento;
        if (o.compareTo(BigDecimal.valueOf(10000)) >= 0) s += 30;
        else if (o.compareTo(BigDecimal.valueOf(5000)) >= 0) s += 15;
        if (prazo == Prazo.MENOS_30) s += 20;
        if (origem == Origem.INDICACAO) s += 20;
        if (decisor) s += 15;
        if (respondeu) s += 10;
        if (etapa.isAberta() && getDiasSemContato() >= 7) s -= 15;
        return s;
    }

    public Prioridade getPrioridade() {
        if (prioridadeManual != null) return prioridadeManual;
        int s = getPontuacao();
        return s >= 70 ? Prioridade.ALTA : s >= 40 ? Prioridade.MEDIA : Prioridade.BAIXA;
    }
}
