package ro.unibuc.hello.service;

import io.micrometer.core.instrument.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.InformationEntity;
import ro.unibuc.hello.data.InformationRepository;
import ro.unibuc.hello.dto.Greeting;
import ro.unibuc.hello.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class GreetingsService {

    @Autowired
    private InformationRepository informationRepository;

    private final AtomicLong counter = new AtomicLong();
    private static final String helloTemplate = "Hello, %s!";
    private static final String informationTemplate = "%s : %s!";

    // ✅ METRICS
    private final Counter helloCounter;
    private final Timer buildInfoTimer;
    private final Gauge greetingCountGauge;
    private final DistributionSummary greetingLengthSummary;
    private final Counter updateCounter;

    public GreetingsService(MeterRegistry registry) {
        helloCounter = registry.counter("custom_hello_requests_total");
        buildInfoTimer = registry.timer("custom_info_request_duration_seconds");
        updateCounter = registry.counter("custom_greeting_updates_total");

        greetingCountGauge = Gauge.builder("custom_greeting_count", this, GreetingsService::getGreetingCount)
                                  .register(registry);

        greetingLengthSummary = DistributionSummary.builder("custom_greeting_content_length")
                                                  .description("Length of greeting content")
                                                  .register(registry);
    }

    public Greeting hello(String name) {
        helloCounter.increment();
        return new Greeting(Long.toString(counter.incrementAndGet()), String.format(helloTemplate, name));
    }

    public Greeting buildGreetingFromInfo(String title) throws EntityNotFoundException {
        return buildInfoTimer.record(() -> {
            InformationEntity entity = informationRepository.findByTitle(title);
            if (entity == null) {
                throw new EntityNotFoundException(title);
            }
            return new Greeting(Long.toString(counter.incrementAndGet()), String.format(informationTemplate, entity.getTitle(), entity.getDescription()));
        });
    }

    public List<Greeting> getAllGreetings() {
        List<InformationEntity> entities = informationRepository.findAll();
        return entities.stream()
                .map(entity -> new Greeting(entity.getId(), entity.getTitle()))
                .collect(Collectors.toList());
    }

    public Greeting saveGreeting(Greeting greeting) {
        InformationEntity entity = new InformationEntity();
        entity.setId(greeting.getId());
        entity.setTitle(greeting.getContent());
        informationRepository.save(entity);

        greetingLengthSummary.record(greeting.getContent().length());

        return new Greeting(entity.getId(), entity.getTitle());
    }

    public Greeting updateGreeting(String id, Greeting greeting) throws EntityNotFoundException {
        InformationEntity entity = informationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.valueOf(id)));
        entity.setTitle(greeting.getContent());
        informationRepository.save(entity);

        updateCounter.increment();

        return new Greeting(entity.getId(), entity.getTitle());
    }

    public void deleteGreeting(String id) throws EntityNotFoundException {
        InformationEntity entity = informationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.valueOf(id)));
        informationRepository.delete(entity);
    }

    public void deleteAllGreetings() {
        informationRepository.deleteAll();
    }

    private double getGreetingCount() {
        return informationRepository.count();
    }
}
