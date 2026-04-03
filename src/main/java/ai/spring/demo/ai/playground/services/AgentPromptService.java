package ai.spring.demo.ai.playground.services;

import ai.spring.demo.ai.playground.data.AgentPrompt;
import ai.spring.demo.ai.playground.repository.AgentPromptRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AgentPromptService {

    private static final Logger log = LoggerFactory.getLogger(AgentPromptService.class);

    private final AgentPromptRepository repository;
    private final Resource defaultPromptResource;
    private final AtomicReference<String> cache = new AtomicReference<>();

    public AgentPromptService(
            AgentPromptRepository repository,
            @Value("classpath:/prompts/system-prompt.st") Resource defaultPromptResource) {
        this.repository = repository;
        this.defaultPromptResource = defaultPromptResource;
    }

    @PostConstruct
    public void init() {
        AgentPrompt active = repository.findByActiveTrue().orElseGet(() -> {
            log.info("No active prompt in DB — seeding from system-prompt.st");
            String content = loadDefaultPrompt();
            AgentPrompt prompt = new AgentPrompt();
            prompt.setContent(content);
            prompt.setDescription("Prompt inicial (importado do arquivo)");
            prompt.setActive(true);
            prompt.setCreatedAt(LocalDateTime.now());
            return repository.save(prompt);
        });
        cache.set(active.getContent());
        log.info("Agent prompt loaded from DB (id={})", active.getId());
    }

    public String getActivePrompt() {
        return cache.get();
    }

    @Transactional
    public AgentPrompt saveNewVersion(String content, String description) {
        // Deactivate current
        repository.findByActiveTrue().ifPresent(p -> {
            p.setActive(false);
            repository.save(p);
        });

        AgentPrompt newPrompt = new AgentPrompt();
        newPrompt.setContent(content);
        newPrompt.setDescription(description != null ? description : "");
        newPrompt.setActive(true);
        newPrompt.setCreatedAt(LocalDateTime.now());
        AgentPrompt saved = repository.save(newPrompt);

        cache.set(content);
        log.info("Agent prompt updated to version id={}", saved.getId());
        return saved;
    }

    @Transactional
    public AgentPrompt activateVersion(Long id) {
        AgentPrompt target = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prompt id=" + id + " not found"));

        repository.findByActiveTrue().ifPresent(p -> {
            p.setActive(false);
            repository.save(p);
        });

        target.setActive(true);
        AgentPrompt saved = repository.save(target);
        cache.set(saved.getContent());
        log.info("Agent prompt rolled back to id={}", id);
        return saved;
    }

    public List<AgentPrompt> listAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    private String loadDefaultPrompt() {
        try (Reader reader = new InputStreamReader(defaultPromptResource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load default system prompt", e);
        }
    }
}
