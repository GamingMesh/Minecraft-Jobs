package me.zford.jobs.spout;

import java.util.logging.Logger;

import org.spout.api.Spout;

import me.zford.jobs.Player;
import me.zford.jobs.Server;

public class SpoutServer implements Server {

    @Override
    public Player getPlayer(String name) {
        return SpoutUtil.wrapPlayer(Spout.getEngine().getPlayer(name, false));
    }

    @Override
    public Player[] getOnlinePlayers() {
        org.spout.api.entity.Player[] players = ((org.spout.api.Server) Spout.getEngine()).getOnlinePlayers();
        Player[] copy = new Player[players.length];
        for (int i=0; i < players.length; i++) {
            copy[i] = SpoutUtil.wrapPlayer(players[i]);
        }
        return copy;
    }

    @Override
    public Logger getLogger() {
        return Spout.getEngine().getLogger();
    }

    @Override
    public void broadcastMessage(String message) {
        ((org.spout.api.Server) Spout.getEngine()).broadcastMessage(message);
    }

}
