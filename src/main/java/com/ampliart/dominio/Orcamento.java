package com.ampliart.dominio;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orcamento")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "cliente_nome", nullable = false)
    private String clienteNome;

    @NotBlank
    @Size(max = 15, message = "Telefone deve ter no maximo 15 caracteres")
    @Column(name = "cliente_telefone", nullable = false)
    private String clienteTelefone;

    @Email
    @Column(name = "cliente_email")
    private String clienteEmail;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusOrcamento status = StatusOrcamento.rascunho;

    @NotNull
    @Column(name = "total_bruto", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalBruto = BigDecimal.ZERO;

    @Column(name = "percentual_ajuste", precision = 5, scale = 2)
    private BigDecimal percentualAjuste;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ajuste", length = 15)
    private TipoAjuste tipoAjuste;

    @Column(name = "valor_ajuste", precision = 19, scale = 2)
    private BigDecimal valorAjuste;

    @NotNull
    @Column(name = "total_final", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalFinal = BigDecimal.ZERO;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrcamentoItem> itens = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        this.dataCadastro = agora;
        this.dataAtualizacao = agora;
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getClienteTelefone() {
        return clienteTelefone;
    }

    public void setClienteTelefone(String clienteTelefone) {
        this.clienteTelefone = clienteTelefone;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public StatusOrcamento getStatus() {
        return status;
    }

    public void setStatus(StatusOrcamento status) {
        this.status = status;
    }

    public BigDecimal getTotalBruto() {
        return totalBruto;
    }

    public void setTotalBruto(BigDecimal totalBruto) {
        this.totalBruto = totalBruto;
    }

    public BigDecimal getPercentualAjuste() {
        return percentualAjuste;
    }

    public void setPercentualAjuste(BigDecimal percentualAjuste) {
        this.percentualAjuste = percentualAjuste;
    }

    public TipoAjuste getTipoAjuste() {
        return tipoAjuste;
    }

    public void setTipoAjuste(TipoAjuste tipoAjuste) {
        this.tipoAjuste = tipoAjuste;
    }

    public BigDecimal getValorAjuste() {
        return valorAjuste;
    }

    public void setValorAjuste(BigDecimal valorAjuste) {
        this.valorAjuste = valorAjuste;
    }

    public BigDecimal getTotalFinal() {
        return totalFinal;
    }

    public void setTotalFinal(BigDecimal totalFinal) {
        this.totalFinal = totalFinal;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }

    public List<OrcamentoItem> getItens() {
        return itens;
    }

    public void setItens(List<OrcamentoItem> itens) {
        this.itens = itens;
    }
}
