package com.ampliart.servico;

import com.ampliart.dominio.*;
import com.ampliart.repo.MovimentacaoEstoqueRepositorio;
import com.ampliart.repo.OrcamentoItemRepositorio;
import com.ampliart.repo.OrcamentoRepositorio;
import com.ampliart.repo.ProdutoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;

@Service
public class OrcamentoServico {

    private final OrcamentoRepositorio orcamentoRepositorio;
    private final OrcamentoItemRepositorio orcamentoItemRepositorio;
    private final ProdutoRepositorio produtoRepositorio;
    private final MovimentacaoEstoqueRepositorio movimentacaoEstoqueRepositorio;

    public OrcamentoServico(OrcamentoRepositorio orcamentoRepositorio,
                            OrcamentoItemRepositorio orcamentoItemRepositorio,
                            ProdutoRepositorio produtoRepositorio,
                            MovimentacaoEstoqueRepositorio movimentacaoEstoqueRepositorio) {
        this.orcamentoRepositorio = orcamentoRepositorio;
        this.orcamentoItemRepositorio = orcamentoItemRepositorio;
        this.produtoRepositorio = produtoRepositorio;
        this.movimentacaoEstoqueRepositorio = movimentacaoEstoqueRepositorio;
    }

    public Orcamento buscarPorId(Long id) {
        return orcamentoRepositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orcamento nao encontrado"));
    }

    @Transactional
    public Orcamento criar(Orcamento orcamento) {
        if (orcamento.getClienteEmail() != null && orcamento.getClienteEmail().isBlank()) {
            orcamento.setClienteEmail(null);
        }
        orcamento.setStatus(StatusOrcamento.rascunho);
        orcamento.setTotalBruto(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        orcamento.setTotalFinal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        return orcamentoRepositorio.save(orcamento);
    }

    @Transactional
    public Orcamento adicionarItemPorCodigo(Long orcamentoId, String codigoProduto, Integer quantidade) {
        Orcamento orcamento = buscarPorId(orcamentoId);
        Produto produto = produtoRepositorio.findByCodigo(codigoProduto)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado para o codigo informado"));

        if (quantidade == null || quantidade < 1) {
            quantidade = 1;
        }

        OrcamentoItem existente = orcamento.getItens().stream()
                .filter(item -> item.getProduto().getId().equals(produto.getId()))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            existente.setQuantidade(existente.getQuantidade() + quantidade);
            existente.setSubtotal(calcularSubtotal(existente.getPrecoUnitario(), existente.getQuantidade()));
            orcamentoItemRepositorio.save(existente);
        } else {
            OrcamentoItem item = new OrcamentoItem();
            item.setOrcamento(orcamento);
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(produto.getPrecoVenda().setScale(2, RoundingMode.HALF_UP));
            item.setSubtotal(calcularSubtotal(item.getPrecoUnitario(), quantidade));
            orcamento.getItens().add(item);
            orcamentoItemRepositorio.save(item);
        }

        recalcularTotais(orcamento);
        return orcamentoRepositorio.save(orcamento);
    }

    @Transactional
    public Orcamento atualizarItem(Long orcamentoId, Long itemId, Integer quantidade, BigDecimal precoUnitario) {
        Orcamento orcamento = buscarPorId(orcamentoId);
        OrcamentoItem item = orcamentoItemRepositorio.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item nao encontrado"));

        if (!item.getOrcamento().getId().equals(orcamento.getId())) {
            throw new IllegalArgumentException("Item nao pertence ao orcamento");
        }

        if (quantidade != null && quantidade > 0) {
            item.setQuantidade(quantidade);
        }
        if (precoUnitario != null && precoUnitario.compareTo(BigDecimal.ZERO) >= 0) {
            item.setPrecoUnitario(precoUnitario.setScale(2, RoundingMode.HALF_UP));
        }

        item.setSubtotal(calcularSubtotal(item.getPrecoUnitario(), item.getQuantidade()));
        orcamentoItemRepositorio.save(item);
        recalcularTotais(orcamento);
        return orcamentoRepositorio.save(orcamento);
    }

    @Transactional
    public Orcamento removerItem(Long orcamentoId, Long itemId) {
        Orcamento orcamento = buscarPorId(orcamentoId);
        OrcamentoItem item = orcamentoItemRepositorio.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item nao encontrado"));
        if (!item.getOrcamento().getId().equals(orcamento.getId())) {
            throw new IllegalArgumentException("Item nao pertence ao orcamento");
        }
        orcamento.getItens().remove(item);
        orcamentoItemRepositorio.delete(item);
        recalcularTotais(orcamento);
        return orcamentoRepositorio.save(orcamento);
    }

    @Transactional
    public Orcamento aplicarAjuste(Long orcamentoId, TipoAjuste tipoAjuste, BigDecimal percentual) {
        Orcamento orcamento = buscarPorId(orcamentoId);
        if (percentual == null) {
            throw new IllegalArgumentException("Informe o percentual do ajuste");
        }
        if (percentual.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Percentual do ajuste nao pode ser negativo");
        }
        if (percentual.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentual do ajuste deve ser no maximo 100%");
        }
        if (percentual.compareTo(BigDecimal.ZERO) == 0) {
            orcamento.setTipoAjuste(null);
            orcamento.setPercentualAjuste(null);
            recalcularTotais(orcamento);
            return orcamentoRepositorio.save(orcamento);
        }
        if (tipoAjuste == null) {
            throw new IllegalArgumentException("Selecione se o ajuste e desconto ou acrescimo");
        }
        percentual = percentual.setScale(2, RoundingMode.HALF_UP);
        orcamento.setTipoAjuste(tipoAjuste);
        orcamento.setPercentualAjuste(percentual);
        recalcularTotais(orcamento);
        return orcamentoRepositorio.save(orcamento);
    }

    @Transactional
    public Orcamento removerAjuste(Long orcamentoId) {
        Orcamento orcamento = buscarPorId(orcamentoId);
        orcamento.setTipoAjuste(null);
        orcamento.setPercentualAjuste(null);
        recalcularTotais(orcamento);
        return orcamentoRepositorio.save(orcamento);
    }

    @Transactional
    public Orcamento alterarStatus(Long orcamentoId, StatusOrcamento novoStatus) {
        Orcamento orcamento = buscarPorId(orcamentoId);
        if (novoStatus == null) {
            throw new IllegalArgumentException("Status invalido");
        }
        StatusOrcamento statusAtual = orcamento.getStatus();
        if (statusAtual == StatusOrcamento.venda_concluida) {
            return orcamento;
        }

        orcamento.setStatus(novoStatus);
        if (novoStatus == StatusOrcamento.venda_concluida) {
            concluirVenda(orcamento);
        }
        return orcamentoRepositorio.save(orcamento);
    }

    @Transactional
    public void concluirVenda(Orcamento orcamento) {
        if (orcamento.getDataConclusao() != null) {
            return;
        }
        orcamento.getItens().sort(Comparator.comparing(i -> i.getProduto().getId()));
        for (OrcamentoItem item : orcamento.getItens()) {
            Produto produto = produtoRepositorio.findById(item.getProduto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
            int novoSaldo = produto.getQuantidadeEstoque() - item.getQuantidade();
            if (novoSaldo < 0) {
                throw new IllegalStateException("Estoque insuficiente para o produto " + produto.getNome());
            }
        }
        for (OrcamentoItem item : orcamento.getItens()) {
            Produto produto = produtoRepositorio.findById(item.getProduto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - item.getQuantidade());
            produtoRepositorio.save(produto);

            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setProduto(produto);
            mov.setTipo(TipoMovimentacaoEstoque.saida);
            mov.setQuantidade(item.getQuantidade());
            mov.setMotivo("Baixa por venda do orcamento " + orcamento.getId());
            movimentacaoEstoqueRepositorio.save(mov);
        }
        orcamento.setDataConclusao(LocalDateTime.now());
    }

    public void recalcularTotais(Orcamento orcamento) {
        BigDecimal totalBruto = orcamento.getItens().stream()
                .map(OrcamentoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        orcamento.setTotalBruto(totalBruto);

        BigDecimal valorAjuste = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (orcamento.getPercentualAjuste() != null && orcamento.getTipoAjuste() != null) {
            valorAjuste = totalBruto
                    .multiply(orcamento.getPercentualAjuste())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (orcamento.getTipoAjuste() == TipoAjuste.desconto) {
                valorAjuste = valorAjuste.negate();
            }
        }
        orcamento.setValorAjuste(valorAjuste);
        BigDecimal totalFinal = totalBruto.add(valorAjuste).setScale(2, RoundingMode.HALF_UP);
        orcamento.setTotalFinal(totalFinal);
    }

    private BigDecimal calcularSubtotal(BigDecimal precoUnitario, Integer quantidade) {
        return precoUnitario.multiply(new BigDecimal(quantidade)).setScale(2, RoundingMode.HALF_UP);
    }
}
