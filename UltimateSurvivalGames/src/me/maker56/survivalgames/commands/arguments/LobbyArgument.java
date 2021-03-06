package me.maker56.survivalgames.commands.arguments;

import me.maker56.survivalgames.SurvivalGames;
import me.maker56.survivalgames.arena.Arena;
import me.maker56.survivalgames.chat.Helper;
import me.maker56.survivalgames.commands.messages.MessageHandler;
import me.maker56.survivalgames.commands.permission.Permission;
import me.maker56.survivalgames.commands.permission.PermissionHandler;
import me.maker56.survivalgames.game.Game;
import me.maker56.survivalgames.game.GameState;
import me.maker56.survivalgames.reset.Reset;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LobbyArgument {
	
	private CommandSender sender;
	private String[] args;
	
	public LobbyArgument(CommandSender sender, String[] args) {
		this.sender = sender;
		this.args = args;
	}
	
	public boolean execute() {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage("&cThe lobby argument can only execute as a Player!");
			return true;
		}
		
		Player p = (Player)sender;
		
		if(!PermissionHandler.hasPermission(p, Permission.GAME) && !PermissionHandler.hasPermission(p, Permission.LOBBY)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("no-permission")));
			return true;
		}
		
		if(args.length == 1) {
			Helper.showLobbyHelpsite(p);
		} else {
			if(args[1].equalsIgnoreCase("delete")) {
				if(args.length == 2) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-must-enter").replace("%0%", "/sg lobby create <NAME>")));
					return true;
				}
				
				if(SurvivalGames.gameManager.getGame(args[2]) != null) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&cYou must unload the lobby first! /sg lobby unload " + args[2]));
					return true;
				}
				
				if(!SurvivalGames.database.contains("Games." + args[2])) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&cLobby " + args[2] + " does not exist!"));
					return true;
				}
				
				SurvivalGames.database.set("Games." + args[2], null);
				SurvivalGames.saveDataBase();
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "You've removed lobby " + args[2] + " successfully!"));
				return true;
			} else if(args[1].equalsIgnoreCase("create")) {
				if(args.length == 2) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-must-enter").replace("%0%", "/sg lobby create <NAME>")));
					return true;
				}
				
				SurvivalGames.gameManager.createGame(p, args[2]);
				return true;
				
				
			} else if(args[1].equalsIgnoreCase("setspawn")) {
				
				if(args.length == 2) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-must-enter").replace("%0%", "/sg lobby setspawn <NAME>")));
					return true;
				}
				
				SurvivalGames.gameManager.setSpawn(p, args[2]);
				return true;
				
				
			} else if(args[1].equalsIgnoreCase("unload")) {
				if(args.length == 2) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-must-enter").replace("%0%", "/sg lobby unload <NAME>")));
					return true;
				}
				Game game = SurvivalGames.gameManager.getGame(args[2]);
				if(game == null) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-not-loaded").replace("%0%", args[2])));
					return true;
				}
				game.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "c&lYour lobby was stopped by an admin!"));
				if(game.getState() == GameState.INGAME || game.getState() == GameState.DEATHMATCH) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&cIt my can be that the blocks of arena " + game.getCurrentArena().getName() + " aren't reseted yet. It will reset while loading lobby."));
				}
				
				SurvivalGames.gameManager.unload(game);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-success-unloaded").replace("%0%", args[2])));
				SurvivalGames.signManager.updateSigns();
				return true;
				
				
			} else if(args[1].equalsIgnoreCase("load")) {
				if(args.length == 2) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-must-enter").replace("%0%", "/sg lobby unload <NAME>")));
					return true;
				}
				Game game = SurvivalGames.gameManager.getGame(args[2]);
				if(game != null) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-already-loaded").replace("%0%", args[2])));
					return true;
				}
				
				boolean success = SurvivalGames.gameManager.load(args[2]);
				
				if(!success) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-load-error").replace("%0%", args[2]).replace("%1%", "See console for informations! It may can be that a few arenas have to be reset. When this happens, the game will automatically load after all arenas were reset.")));
				} else {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-success-loaded").replace("%0%", args[2])));
					SurvivalGames.signManager.updateSigns();
				}
				return true;
				
				
			} else if(args[1].equalsIgnoreCase("reload")) {
				if(args.length == 2) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-must-enter").replace("%0%", "/sg lobby unload <NAME>")));
					return true;
				}
				//lobby=0
                //reload=1
                //Lname=2
                //Aname=3
				if (args.length==3){
                    p.performCommand("sg lobby unload " + args[2]);
                    p.performCommand("sg lobby load "+ args[2]);
                    return true;
                }
				if (args.length == 4) {
                    Game game = SurvivalGames.getGameManager().getGame(args[2]);
                    Arena arena = SurvivalGames.getArenaManager().getArena(args[2], args[3]);
                    if (!arena.getName().isEmpty()) {
                        game.kickall();
                        game.setState(GameState.RESET);
                        World w = arena.getMinimumLocation().getWorld();
                        if (game.isResetEnabled()) {
                            new Reset(w, game.getName(), arena.getName(), game.getChunksToReset()).start();
                            return true;
                        } else {
                            String name = game.getName();
                            SurvivalGames.gameManager.unload(game);
                            SurvivalGames.gameManager.load(name);
                            SurvivalGames.signManager.updateSigns();
                        }
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix")+"Reload of "+args[3]+" in "+ args[2] +" - Complete"));
                        return true;
                    }
                }


				return true;
				
				
			} else if(args[1].equalsIgnoreCase("list")) {
				if(args.length == 2) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-must-enter").replace("%0%", "/sg lobby list <NAME>")));
					return true;
				}
				Game game = SurvivalGames.gameManager.getGame(args[2]);
				if(game == null) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("game-not-loaded").replace("%0%", args[2])));
					return true;
				}{
				List<Arena> arenas = game.getArenas();
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "Arenas in lobby " + game.getName() + "&8: &7(&b" + arenas.size() + "&7)"));
				for(Arena a : arenas)
					p.sendMessage("&7- &6" + a.getName());
				}
				return true;
			}
			
			
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MessageHandler.getMessage("prefix") + "&cCommand not found! Type /sg lobby for help!"));
			return true;
			
		}
		
		
		return true;
		
	}

}
