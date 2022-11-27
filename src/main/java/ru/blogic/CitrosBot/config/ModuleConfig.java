package ru.blogic.CitrosBot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.module.Module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Конфигурация модулей приложения
 *
 * @author eyakimov
 */
@Configuration
public class ModuleConfig {

    @Autowired
    private List<Module> modules;

    @Bean
    public Map<ModuleEnum, Module> systemModules() {
        Map<ModuleEnum, Module> systemModules = new HashMap<>();
        for (Module module : modules) {
            systemModules.put(module.getModuleType(), module);
        }
        return systemModules;
    }
}
