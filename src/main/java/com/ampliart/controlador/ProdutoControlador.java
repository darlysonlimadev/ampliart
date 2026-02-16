package com.ampliart.controlador;

import com.ampliart.dominio.Produto;
import com.ampliart.servico.CategoriaServico;
import com.ampliart.servico.ProdutoServico;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/produtos")
public class ProdutoControlador {

    private final ProdutoServico produtoServico;
    private final CategoriaServico categoriaServico;

    public ProdutoControlador(ProdutoServico produtoServico, CategoriaServico categoriaServico) {
        this.produtoServico = produtoServico;
        this.categoriaServico = categoriaServico;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String nome,
                         @RequestParam(required = false) String codigo,
                         @RequestParam(required = false) Boolean ativo,
                         @RequestParam(required = false) String nomeProduto,
                         @RequestParam(required = false) Long categoriaId,
                         Model model) {
        boolean usarBuscaNova = (nomeProduto != null && !nomeProduto.isBlank()) || categoriaId != null;
        List<Produto> produtos = usarBuscaNova
                ? produtoServico.listarPorNomeECategoria(nomeProduto, categoriaId)
                : produtoServico.listar(nome, codigo, ativo);
        Map<Long, String> margensPorProduto = produtos.stream()
                .collect(Collectors.toMap(Produto::getId, this::calcularMargem));
        model.addAttribute("produtos", produtos);
        model.addAttribute("margensPorProduto", margensPorProduto);
        model.addAttribute("categorias", categoriaServico.listar());
        model.addAttribute("nomeProduto", nomeProduto);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("nome", nome);
        model.addAttribute("codigo", codigo);
        model.addAttribute("ativo", ativo);
        return "produtos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", new Produto());
        model.addAttribute("categorias", categoriaServico.listarParaCadastroProduto());
        return "produtos/formulario";
    }

    @PostMapping("/novo")
    public String criar(@Valid @ModelAttribute("produto") Produto produto,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaServico.listarParaCadastroProduto());
            return "produtos/formulario";
        }
        try {
            produtoServico.salvar(produto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto cadastrado com sucesso");
            return "redirect:/produtos";
        } catch (IllegalArgumentException ex) {
            aplicarErroDeFormulario(bindingResult, ex.getMessage());
            model.addAttribute("categorias", categoriaServico.listarParaCadastroProduto());
            return "produtos/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Produto produto = produtoServico.buscarPorId(id);
        if (categoriaServico.ehCategoriaPadrao(produto.getCategoria())) {
            produto.setCategoria(null);
        }
        model.addAttribute("produto", produto);
        model.addAttribute("categorias", categoriaServico.listarParaCadastroProduto());
        return "produtos/formulario";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("produto") Produto produto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaServico.listarParaCadastroProduto());
            return "produtos/formulario";
        }
        produto.setId(id);
        try {
            produtoServico.salvar(produto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto atualizado com sucesso");
            return "redirect:/produtos";
        } catch (IllegalArgumentException ex) {
            aplicarErroDeFormulario(bindingResult, ex.getMessage());
            model.addAttribute("categorias", categoriaServico.listarParaCadastroProduto());
            return "produtos/formulario";
        }
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        Produto produto = produtoServico.buscarPorId(id);
        model.addAttribute("produto", produto);
        return "produtos/detalhe";
    }

    private String calcularMargem(Produto produto) {
        BigDecimal custo = produto.getPrecoCompra();
        if (custo == null || custo.compareTo(BigDecimal.ZERO) <= 0) {
            return "-";
        }
        BigDecimal margem = produto.getPrecoVenda()
                .subtract(custo)
                .multiply(BigDecimal.valueOf(100))
                .divide(custo, 0, RoundingMode.HALF_UP);
        return margem + "%";
    }

    private void aplicarErroDeFormulario(BindingResult bindingResult, String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            bindingResult.reject("produto.erro", "Nao foi possivel salvar o produto");
            return;
        }

        String mensagemNormalizada = mensagem.toLowerCase();
        if (mensagemNormalizada.contains("categoria")) {
            bindingResult.rejectValue("categoria.id", "", mensagem);
            return;
        }
        if (mensagemNormalizada.contains("codigo")) {
            bindingResult.rejectValue("codigo", "", mensagem);
            return;
        }
        bindingResult.reject("produto.erro", mensagem);
    }
}
