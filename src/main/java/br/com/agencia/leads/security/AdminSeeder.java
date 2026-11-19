package br.com.agencia.leads.security;

import br.com.agencia.leads.model.UserAccount;
import br.com.agencia.leads.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private final UserRepository users;
    private final PasswordEncoder encoder;
    @Value("${app.admin.nome}") private String nome;
    @Value("${app.admin.email}") private String email;
    @Value("${app.admin.senha}") private String senha;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;
        UserAccount u = new UserAccount();
        u.setNome(nome);
        u.setEmail(email.toLowerCase());
        u.setSenhaHash(encoder.encode(senha));
        u.setRole(UserAccount.Role.ADMIN);
        users.save(u);
        log.warn("Administrador criado: {}. Troque a senha padrão no primeiro acesso.", email);
    }
}
