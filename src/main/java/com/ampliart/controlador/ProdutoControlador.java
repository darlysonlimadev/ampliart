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

import java.util.List;

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
                         Model model) {
        List<Produto> produtos = produtoServico.listar(nome, codigo, ativo);
        model.addAttribute("produtos", produtos);
        model.addAttribute("nome", nome);
        model.addAttribute("codigo", codigo);
        model.addAttribute("ativo", ativo);
        return "produtos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", new Produto());
        model.addAttribute("categorias", categoriaServico.listar());
        return "produtos/formulario";
    }

    @PostMapping("/novo")
    public String criar(@Valid @ModelAttribute("produto") Produto produto,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaServico.listar());
            return "produtos/formulario";
        }
        try {
            produtoServico.salvar(produto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto cadastrado com sucesso");
            return "redirect:/produtos";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("codigo", "", ex.getMessage());
            model.addAttribute("categorias", categoriaServico.listar());
            return "produtos/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Produto produto = produtoServico.buscarPorId(id);
        model.addAttribute("produto", produto);
        model.addAttribute("categorias", categoriaServico.listar());
        return "produtos/formulario";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("produto") Produto produto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaServico.listar());
            return "produtos/formulario";
        }
        produto.setId(id);
        try {
            produtoServico.salvar(produto);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto atualizado com sucesso");
            return "redirect:/produtos";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("codigo", "", ex.getMessage());
            model.addAttribute("categorias", categoriaServico.listar());
            return "produtos/formulario";
        }
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        Produto produto = produtoServico.buscarPorId(id);
        model.addAttribute("produto", produto);
        return "produtos/detalhe";
    }
}
