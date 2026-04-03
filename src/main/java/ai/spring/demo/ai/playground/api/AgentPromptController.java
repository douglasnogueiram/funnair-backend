package ai.spring.demo.ai.playground.api;

import ai.spring.demo.ai.playground.data.AgentPrompt;
import ai.spring.demo.ai.playground.services.AgentPromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent-prompt")
public class AgentPromptController {

    private final AgentPromptService service;

    public AgentPromptController(AgentPromptService service) {
        this.service = service;
    }

    public record SaveRequest(String content, String description) {}

    @GetMapping("/active")
    public ResponseEntity<AgentPrompt> getActive() {
        String content = service.getActivePrompt();
        AgentPrompt prompt = new AgentPrompt();
        prompt.setContent(content);
        return ResponseEntity.ok(prompt);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AgentPrompt>> history() {
        return ResponseEntity.ok(service.listAll());
    }

    @PostMapping
    public ResponseEntity<AgentPrompt> save(@RequestBody SaveRequest req) {
        return ResponseEntity.ok(service.saveNewVersion(req.content(), req.description()));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<AgentPrompt> activate(@PathVariable Long id) {
        return ResponseEntity.ok(service.activateVersion(id));
    }
}
