package br.com.agencia.leads.web;

import br.com.agencia.leads.model.UserAccount;
import br.com.agencia.leads.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/usuarios")
public class UserController {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping
    public String lista(Model m) {
        m.addAttribute("lista", users.findAll());
        m.addAttribute("roles", UserAccount.Role.values());
        return "users";
    }

    @PostMapping
    public String criar(@RequestParam String nome, @RequestParam String email, @RequestParam String senha,
                        @RequestParam UserAccount.Role role, RedirectAttributes ra) {
        if (nome.isBlank() || email.isBlank() || senha.length() < 8) {
            ra.addFlashAttribute("erro", "Preencha nome e e-mail e use uma senha com pelo menos 8 caracteres.");
        } else if (users.findByEmailIgnoreCase(email.trim()).isPresent()) {
            ra.addFlashAttribute("erro", "Já existe um usuário com este e-mail.");
        } else {
            UserAccount u = new UserAccount();
            u.setNome(nome.trim());
            u.setEmail(email.trim().toLowerCase());
            u.setSenhaHash(encoder.encode(senha));
            u.setRole(role);
            users.save(u);
            ra.addFlashAttribute("ok", "Usuário criado.");
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/alternar")
    public String alternar(@PathVariable Long id, Principal p, RedirectAttributes ra) {
        UserAccount u = users.findById(id).orElseThrow();
        if (u.getEmail().equalsIgnoreCase(p.getName())) {
            ra.addFlashAttribute("erro", "Você não pode desativar o seu próprio acesso.");
        } else {
            u.setAtivo(!u.isAtivo());
            users.save(u);
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/senha")
    public String senha(@PathVariable Long id, @RequestParam String senha, RedirectAttributes ra) {
        if (senha.length() < 8) {
            ra.addFlashAttribute("erro", "A senha precisa ter pelo menos 8 caracteres.");
        } else {
            UserAccount u = users.findById(id).orElseThrow();
            u.setSenhaHash(encoder.encode(senha));
            users.save(u);
            ra.addFlashAttribute("ok", "Senha atualizada.");
        }
        return "redirect:/usuarios";
    }
}
