package cc.azuramc.bedwars.game.timeline;

import cc.azuramc.bedwars.config.ConfigValidator;
import cc.azuramc.bedwars.config.object.GeneratorConfig;
import cc.azuramc.bedwars.config.object.TimelineConfig;
import cc.azuramc.bedwars.shop.SpecChecks;

import java.util.Map;

/**
 * Checks the stages of timeline.yml and their references to generators.yml
 *
 * @author an5w1r@163.com
 */
public final class TimelineValidator implements ConfigValidator {

    private final SpecChecks.Names names;

    public TimelineValidator(SpecChecks.Names names) {
        this.names = names;
    }

    @Override
    public void validate(View view, Sink sink) {
        String document = view.idOf(TimelineConfig.class);
        Map<String, GeneratorConfig.Resource> resources = view.get(GeneratorConfig.class).getResources();
        boolean ends = false;
        for (int i = 0; i < view.get(TimelineConfig.class).getStages().size(); i++) {
            TimelineConfig.Stage stage = view.get(TimelineConfig.class).getStages().get(i);
            for (int j = 0; j < stage.getActions().size(); j++) {
                String path = "stages[" + i + "].actions[" + j + "]";
                TimelineConfig.Action action = stage.getActions().get(j);
                ends |= TimelineConfig.Action.END_GAME.equals(action.getType());
                check(document, path, action, resources, sink);
            }
        }
        if (!ends) {
            sink.warn(document, "stages", "No stage has an end-game action, a game only ends when one team is left");
        }
    }

    private void check(String document, String path, TimelineConfig.Action action, Map<String, GeneratorConfig.Resource> resources, Sink sink) {
        switch (action.getType()) {
            case TimelineConfig.Action.GENERATOR_LEVEL:
                GeneratorConfig.Resource resource = action.getResource() == null ? null : resources.get(action.getResource());
                if (action.getResource() == null) {
                    sink.error(document, path + ".resource", "A generator-level action needs resource, an id of generators.yml");
                } else if (resource == null) {
                    sink.error(document, path + ".resource", SpecChecks.unknown("resource", action.getResource(), resources.keySet()));
                } else if (resource.getAt() != GeneratorConfig.Spawn.PUBLIC) {
                    sink.error(document, path + ".resource", "Only a PUBLIC resource has a shared level, " + action.getResource() + " is " + resource.getAt());
                } else if (action.getLevel() == null || action.getLevel() > resource.getLevels().size()) {
                    sink.error(document, path + ".level", "Write a level from 1 to " + resource.getLevels().size() + ", the levels of " + action.getResource());
                }
                break;
            case TimelineConfig.Action.TITLE:
                if (action.getTitle() == null && action.getSubtitle() == null) {
                    sink.error(document, path + ".title", "A title action needs title or subtitle");
                }
                break;
            case TimelineConfig.Action.SOUND:
                if (action.getSound() == null) {
                    sink.error(document, path + ".sound", "A sound action needs sound, such as ENTITY_ENDER_DRAGON_GROWL");
                } else if (!names.sound(action.getSound())) {
                    sink.warn(document, path + ".sound", "Unknown sound " + action.getSound() + " on this server, it is left out");
                }
                break;
            case TimelineConfig.Action.MESSAGE:
                if (action.getMessage() == null || action.getMessage().isEmpty()) {
                    sink.error(document, path + ".message", "A message action needs message lines");
                }
                break;
            case TimelineConfig.Action.COMMAND:
                if (action.getCommands() == null || action.getCommands().isEmpty()) {
                    sink.error(document, path + ".commands", "A command action needs commands");
                }
                break;
            case TimelineConfig.Action.DESTROY_BEDS:
            case TimelineConfig.Action.END_GAME:
                break;
            default:
                sink.error(document, path + ".type", SpecChecks.unknown("action type", action.getType(), TimelineConfig.Action.TYPES));
                break;
        }
    }
}
