/*
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011  Zak Ford <zak.j.ford@gmail.com>
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
 * 
 */

package com.zford.jobs.economy.link;

/**
 * Interface that to be used when creating a class that talks with the 
 * economy systems (e.g. iConomy, BOSEconomy ...)
 * @author Alex
 *
 */
public interface EconomyLink {
	/**
	 * Pay the player
	 * @param player - player to be paid
	 * @param amount - amount to be paid
	 */
	public void pay(String playername, double amount);
}
