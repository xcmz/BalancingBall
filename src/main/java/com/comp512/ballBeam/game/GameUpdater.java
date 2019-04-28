package com.comp512.ballBeam.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameUpdater {
    private static final Logger logger = LoggerFactory.getLogger(GameUpdater.class);
    private List<Action> actions_x_4;
    private List<Action> actions_x_3;
    private List<Action> actions_x_2;
    private List<Action> actions_x_1;
    private List<Action> actions_x;

    private TempGameState startState;

    public GameUpdater(TempGameState state) {
        actions_x = new ArrayList<>();
        actions_x_1 = new ArrayList<>();
        actions_x_2 = new ArrayList<>();
        actions_x_3 = new ArrayList<>();
        actions_x_4 = new ArrayList<>();
        this.startState = state;
    }

    public void addAction(int clientSyncID, int syncID, Action action) {
        switch (syncID - clientSyncID) {
            case 0:
                actions_x.add(action);
                break;
            case 1:
                actions_x_1.add(action);
                break;
            case 2:
                actions_x_2.add(action);
                break;
            case 3:
                actions_x_3.add(action);
                break;
            case 4:
                actions_x_4.add(action);
                break;
        }
    }

    public void    updateGame(long frameProceed, BallBeamSys ballBeamSys, double controlWeight) {
//        logUpdate();

        ballBeamSys.restoreState(startState);
        // proceed game to consume actions_x_4
        consume(frameProceed, ballBeamSys, actions_x_4, controlWeight);

        // the next start state
        startState = ballBeamSys.saveState();
        consume(frameProceed, ballBeamSys, actions_x_3, controlWeight);
        consume(frameProceed, ballBeamSys, actions_x_2, controlWeight);
        consume(frameProceed, ballBeamSys, actions_x_1, controlWeight);
        consume(frameProceed, ballBeamSys, actions_x, controlWeight);

        actions_x_4.clear();
        actions_x_4.addAll(actions_x_3);
        actions_x_3.clear();
        actions_x_3.addAll(actions_x_2);
        actions_x_2.clear();
        actions_x_2.addAll(actions_x_1);
        actions_x_1.clear();
        actions_x_1.addAll(actions_x);
        actions_x.clear();
    }

    private void logUpdate() {
        StringBuilder sb = new StringBuilder();
        int[] sizes = new int[]{actions_x_4.size(), actions_x_3.size(), actions_x_2.size(), actions_x_1.size(), actions_x.size()};
        sb.append("Action size: ").append(Arrays.toString(sizes));
        logger.info(sb.toString());
    }

    private void consume(long frameProceed, BallBeamSys ballBeamSys, List<Action> actions, double controlWeight) {
        actions.sort(Comparator.comparingInt(action -> action.frameID));
        int k = 0;
        for (int i = 0; i < frameProceed; i++) {
            while (k < actions.size() && actions.get(k).frameID == i) {
                ballBeamSys.rotate(actions.get(k).direction * controlWeight);
                k++;
            }
            ballBeamSys.nextFrame();
        }
    }
}
