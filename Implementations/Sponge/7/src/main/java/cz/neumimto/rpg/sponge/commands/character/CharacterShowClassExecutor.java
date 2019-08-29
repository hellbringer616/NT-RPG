package cz.neumimto.rpg.sponge.commands.character;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.gui.Gui;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Singleton
public class CharacterShowClassExecutor implements CommandExecutor {

    @Inject
    private SpongeCharacterService characterService;
    
    
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<ClassDefinition> classDefinitionOptional = args.getOne("class");
        if (classDefinitionOptional.isPresent()) {
            ClassDefinition clazz = classDefinitionOptional.get();
            if (src instanceof Player) {
                IActiveCharacter character = characterService.getCharacter((Player) src);
                Gui.showClassInfo(character, clazz);
            } else {
                src.sendMessage(Text.of("Only for players!"));
            }
        } else {
            if (src instanceof Player) {
                IActiveCharacter character = characterService.getCharacter((Player) src);
                if (character.getPrimaryClass() != null) {
                    Gui.showClassInfo(character, character.getPrimaryClass().getClassDefinition());
                } else {
                    Gui.sendClassTypes(character);
                }
            } else {
                src.sendMessage(Text.of("Only for players!"));
            }
        }
        return CommandResult.success();
    }
}