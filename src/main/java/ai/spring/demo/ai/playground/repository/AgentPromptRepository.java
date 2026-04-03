package ai.spring.demo.ai.playground.repository;

import ai.spring.demo.ai.playground.data.AgentPrompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentPromptRepository extends JpaRepository<AgentPrompt, Long> {

    Optional<AgentPrompt> findByActiveTrue();

    List<AgentPrompt> findAllByOrderByCreatedAtDesc();
}
