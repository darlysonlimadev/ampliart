package com.ampliart.dominio;

public enum StatusOrcamento {
    rascunho("Rascunho"),
    enviado("Enviado"),
    aguardando_aprovacao("Aguardando Aprovacao"),
    venda_concluida("Venda Concluida"),
    cancelado("Cancelado");

    private final String label;

    StatusOrcamento(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
