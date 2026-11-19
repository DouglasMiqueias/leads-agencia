package br.com.agencia.leads.repo;

import br.com.agencia.leads.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
}
