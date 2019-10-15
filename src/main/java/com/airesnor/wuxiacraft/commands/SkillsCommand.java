package com.airesnor.wuxiacraft.commands;

import com.airesnor.wuxiacraft.capabilities.SkillsProvider;
import com.airesnor.wuxiacraft.cultivation.skills.ISkillCap;
import com.airesnor.wuxiacraft.cultivation.skills.Skill;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SkillsCommand extends CommandBase {
    public SkillsCommand() {
        super();
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("skills");
        return aliases;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public String getName() {
        return "skills";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/skills";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            if(!player.world.isRemote) {
                if(args.length == 0) {
                    ISkillCap skillCap = player.getCapability(SkillsProvider.SKILL_CAP_CAPABILITY, null);
                    if(skillCap == null) {
                        TextComponentString text = new TextComponentString("There is no way to learn skills yet.");
                        sender.sendMessage(text);
                    } else {
                        if(skillCap.getKnownSkills().isEmpty()) {
                            TextComponentString text = new TextComponentString("You don't know any skill yet.");
                            sender.sendMessage(text);
                        } else {
                            TextComponentString text = new TextComponentString("Known skills: ");
                            sender.sendMessage(text);
                            if (skillCap.getKnownSkills() != null) {
                                for (Skill skill : skillCap.getKnownSkills()) {
                                    text = new TextComponentString("Skill: " + skill.getName());
                                    sender.sendMessage(text);
                                }
                            }
                        }
                    }
                }
                else {
                    TextComponentString text = new TextComponentString("Invalid arguments, use /cult target_player");
                    text.getStyle().setColor(TextFormatting.RED);
                    sender.sendMessage(text);
                }
            }
        }
    }
}