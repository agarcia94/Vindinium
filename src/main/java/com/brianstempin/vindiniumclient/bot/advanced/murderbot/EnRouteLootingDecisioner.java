package com.brianstempin.vindiniumclient.bot.advanced.murderbot;

import com.brianstempin.vindiniumclient.bot.BotMove;
import com.brianstempin.vindiniumclient.bot.BotUtils;
import com.brianstempin.vindiniumclient.bot.advanced.AdvancedGameState;
import com.brianstempin.vindiniumclient.bot.advanced.Mine;
import com.brianstempin.vindiniumclient.bot.advanced.Vertex;
import com.brianstempin.vindiniumclient.dto.GameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.List;

/**
 * Decides if we should take some mines on the way to whatever it was we were doing.
 *
 * This decisioner will decide if its worth going after a near-by mine.
 *
 * Maslov says, "self actualization."
 */
public class EnRouteLootingDecisioner implements Decision<AdvancedMurderBot.GameContext, BotMove> {

    private final static Logger logger = LogManager.getLogger(EnRouteLootingDecisioner.class);

    private final Decision<AdvancedMurderBot.GameContext, BotMove> noGoodMineDecisioner;

    public EnRouteLootingDecisioner(Decision<AdvancedMurderBot.GameContext, BotMove> noGoodMineDecisioner) {
        this.noGoodMineDecisioner = noGoodMineDecisioner;
    }

    @Override
    public BotMove makeDecision(AdvancedMurderBot.GameContext context) {
        GameState.Position myPosition = context.getGameState().getMe().getPos();
        Map<GameState.Position, Vertex> boardGraph = context.getGameState().getBoardGraph();

        // Are we next to a mine that isn't ours?
        for(Vertex currentVertex : boardGraph.get(myPosition).getAdjacentVertices()) {
            Mine mine = context.getGameState().getMines().get(currentVertex.getPosition());
            if(mine != null && (mine.getOwner() == null
                    || mine.getOwner().getId() != context.getGameState().getMe().getId()
                    && mine.getOwner().getLife() < context.getGameState().getMe().getLife())) {

                //determine to take mine from other hero if you have larger health
                List<GameState.Hero> enemies = BotUtils.getHeroesAround(context.getGameState(), context.getDijkstraResultMap(), 1);
                boolean myHealthLarger = false;

                for(GameState.Hero enemy : enemies){
                  if(context.getGameState().getMe().getLife() > enemy.getLife()){
                    myHealthLarger = true;
                    break;
                  }

                }

                // Is it safe to take?
                if(BotUtils.getHeroesAround(context.getGameState(), context.getDijkstraResultMap(), 1).size() > 0
                   && !myHealthLarger) {

                    logger.info("Mine found, but another hero is too close.");
                    return noGoodMineDecisioner.makeDecision(context);
                }
                logger.info("Taking a mine that we happen to already be walking by.");
                return BotUtils.directionTowards(myPosition, mine.getPosition());
            }
            /*
            else if(mine != null &&
                    (mine.getOwner() != null &&
                    mine.getOwner().getId() != context.getGameState().getMe().getId() &&
                    context.getGameState().getMe().getLife() >= 35)){

                      // Is it safe to take?
                      if(BotUtils.getHeroesAround(context.getGameState(), context.getDijkstraResultMap(), 1).size() > 0) {
                          logger.info("Mine found, but another hero is too close.");
                          return noGoodMineDecisioner.makeDecision(context);
                      }
                      logger.info("Taking a mine that we happen to already be walking by.");
                      return BotUtils.directionTowards(myPosition, mine.getPosition());

            }
            */


        }

        // Nope.
        logger.info("No opportunistic mines exist.");
        return noGoodMineDecisioner.makeDecision(context);
    }
}
