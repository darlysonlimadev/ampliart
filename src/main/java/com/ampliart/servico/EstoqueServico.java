package com.ampliart.servico;

import com.ampliart.dominio.MovimentacaoEstoque;
import com.ampliart.dominio.Produto;
import com.ampliart.dominio.TipoMovimentacaoEstoque;
import com.ampliart.repo.MovimentacaoEstoqueRepositorio;
import com.ampliart.repo.ProdutoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstoqueServico {

    private final ProdutoRepositorio produtoRepositorio;
    private final MovimentacaoEstoqueRepositorio movimentacaoEstoqueRepositorio;

    public EstoqueServico(ProdutoRepositorio produtoRepositorio,
                          MovimentacaoEstoqueRepositorio movimentacaoEstoqueRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
        this.movimentacaoEstoqueRepositorio = movimentacaoEstoqueRepositorio;
    }

    public List<MovimentacaoEstoque> listarMovimentacoes() {
        return movimentacaoEstoqueRepositorio.findAll();
    }

    @Transactional
    public MovimentacaoEstoque registrarMovimentacao(Long produtoId,
                                                     TipoMovimentacaoEstoque tipo,
                                                     Integer quantidade,
                                                     String motivo) {
        if (quantidade == null || quantidade < 1) {
            throw new IllegalArgumentException("Quantidade invalida");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo obrigatorio");
        }
        Produto produto = produtoRepositorio.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));

        int novoSaldo = produto.getQuantidadeEstoque();
        if (tipo == TipoMovimentacaoEstoque.entrada) {
            novoSaldo += quantidade;
        } else {
            novoSaldo -= quantidade;
            if (novoSaldo < 0) {
                throw new IllegalStateException("Estoque insuficiente para o produto " + produto.getNome());
            }
        }
        produto.setQuantidadeEstoque(novoSaldo);
        produtoRepositorio.save(produto);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProduto(produto);
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        mov.setMotivo(motivo);
        return movimentacaoEstoqueRepositorio.save(mov);
    }
}
