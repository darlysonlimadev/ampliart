package com.ampliart.controlador;

import com.ampliart.dominio.MovimentacaoEstoque;
import com.ampliart.dominio.TipoMovimentacaoEstoque;
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

    public EstoqueControlador(EstoqueServico estoqueServico, ProdutoServico produtoServico) {
        this.estoqueServico = estoqueServico;
        this.produtoServico = produtoServico;
    }

    @GetMapping("/movimentacoes")
    public String movimentacoes(Model model) {
        List<MovimentacaoEstoque> movimentacoes = estoqueServico.listarMovimentacoes();
        model.addAttribute("movimentacoes", movimentacoes);
        model.addAttribute("produtos", produtoServico.listar(null, null, null));
        model.addAttribute("tipos", TipoMovimentacaoEstoque.values());
        return "estoque/movimentacoes";
    }

    @PostMapping("/movimentacoes")
    public String registrar(@RequestParam Long produtoId,
                            @RequestParam TipoMovimentacaoEstoque tipo,
                            @RequestParam Integer quantidade,
                            @RequestParam String motivo,
                            RedirectAttributes redirectAttributes) {
        try {
            estoqueServico.registrarMovimentacao(produtoId, tipo, quantidade, motivo);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimentacao registrada");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/estoque/movimentacoes";
    }
}
