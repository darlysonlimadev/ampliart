package com.ampliart.controlador;

import com.ampliart.dominio.MovimentacaoEstoque;
import com.ampliart.dominio.TipoMovimentacaoEstoque;
import com.ampliart.servico.CategoriaServico;
import com.ampliart.servico.EstoqueServico;
import com.ampliart.servico.ProdutoServico;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/estoque")
public class EstoqueControlador {

    private final EstoqueServico estoqueServico;
    private final ProdutoServico produtoServico;
    private final CategoriaServico categoriaServico;

    public EstoqueControlador(EstoqueServico estoqueServico,
                              ProdutoServico produtoServico,
                              CategoriaServico categoriaServico) {
        this.estoqueServico = estoqueServico;
        this.produtoServico = produtoServico;
        this.categoriaServico = categoriaServico;
    }

    @GetMapping("/movimentacoes")
    public String movimentacoes(@RequestParam(required = false) String nomeProduto,
                                @RequestParam(required = false) Long categoriaId,
                                Model model) {
        List<MovimentacaoEstoque> movimentacoes = estoqueServico.listarMovimentacoes();
        model.addAttribute("movimentacoes", movimentacoes);
        model.addAttribute("produtos", produtoServico.listarPorNomeECategoria(nomeProduto, categoriaId));
        model.addAttribute("categorias", categoriaServico.listar());
        model.addAttribute("nomeProduto", nomeProduto);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("tipos", TipoMovimentacaoEstoque.values());
        return "estoque/movimentacoes";
    }

    @PostMapping("/movimentacoes")
    public String registrar(@RequestParam Long produtoId,
                            @RequestParam TipoMovimentacaoEstoque tipo,
                            @RequestParam Integer quantidade,
                            @RequestParam String motivo,
                            @RequestParam(required = false) String nomeProduto,
                            @RequestParam(required = false) Long categoriaId,
                            RedirectAttributes redirectAttributes) {
        try {
            estoqueServico.registrarMovimentacao(produtoId, tipo, quantidade, motivo);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimentacao registrada");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        if (nomeProduto != null && !nomeProduto.isBlank()) {
            redirectAttributes.addAttribute("nomeProduto", nomeProduto);
        }
        if (categoriaId != null) {
            redirectAttributes.addAttribute("categoriaId", categoriaId);
        }
        return "redirect:/estoque/movimentacoes";
    }
}
