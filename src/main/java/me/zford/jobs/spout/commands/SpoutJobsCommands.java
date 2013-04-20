/**
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011 Zak Ford <zak.j.ford@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.zford.jobs.spout.commands;

import java.util.List;

import me.zford.jobs.commands.JobsCommands;
import me.zford.jobs.spout.SpoutUtil;

import org.spout.api.chat.ChatSection;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandExecutor;
import org.spout.api.command.CommandSource;
import org.spout.api.exception.CommandException;

public class SpoutJobsCommands extends JobsCommands implements CommandExecutor {
    public void jobs(CommandContext args, CommandSource source) throws CommandException {
        List<ChatSection> listArgs = args.getRawArgs();
        String[] arrayArgs = new String[args.length()];
        int i = 0;
        for (ChatSection arg : listArgs) {
            arrayArgs[i] = arg.getPlainString();
            i++;
        }
        
        onCommand(SpoutUtil.wrapCommandSource(source), args.getCommand(), arrayArgs);
    }

    @Override
    public void processCommand(CommandSource source, Command command, CommandContext args) throws CommandException {
        List<ChatSection> listArgs = args.getRawArgs();
        String[] arrayArgs = new String[args.length()];
        int i = 0;
        for (ChatSection arg : listArgs) {
            arrayArgs[i] = arg.getPlainString();
            i++;
        }
        
        onCommand(SpoutUtil.wrapCommandSource(source), command.getPreferredName(), arrayArgs);
    }
}
