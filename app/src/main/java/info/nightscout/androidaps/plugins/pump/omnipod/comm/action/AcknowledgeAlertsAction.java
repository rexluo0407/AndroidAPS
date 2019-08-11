package info.nightscout.androidaps.plugins.pump.omnipod.comm.action;

import java.util.Collections;

import info.nightscout.androidaps.plugins.pump.omnipod.comm.OmnipodCommunicationService;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.command.AcknowledgeAlertsCommand;
import info.nightscout.androidaps.plugins.pump.omnipod.comm.message.response.StatusResponse;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertSet;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.AlertSlot;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.state.PodSessionState;

public class AcknowledgeAlertsAction implements OmnipodAction<StatusResponse> {
    private final PodSessionState podState;
    private final AlertSet alerts;

    public AcknowledgeAlertsAction(PodSessionState podState, AlertSet alerts) {
        if (podState == null) {
            throw new IllegalArgumentException("Pod state cannot be null");
        }
        if (alerts == null) {
            throw new IllegalArgumentException("Alert set can not be null");
        } else if (alerts.size() == 0) {
            throw new IllegalArgumentException("Alert set can not be empty");
        }
        this.podState = podState;
        this.alerts = alerts;
    }

    public AcknowledgeAlertsAction(PodSessionState podState, AlertSlot alertSlot) {
        this(podState, new AlertSet(Collections.singletonList(alertSlot)));
    }

    @Override
    public StatusResponse execute(OmnipodCommunicationService communicationService) {
        return communicationService.sendCommand(StatusResponse.class, podState,
                new AcknowledgeAlertsCommand(podState.getCurrentNonce(), alerts));
    }
}
