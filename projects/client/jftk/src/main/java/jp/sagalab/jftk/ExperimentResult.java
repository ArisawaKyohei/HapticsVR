package jp.sagalab.jftk;

import jp.sagalab.jftk.ExperimentSetting;

public final class ExperimentResult {
    public ExperimentResult(ExperimentSetting _experimentSetting, Double _timeElapsed) {
        experimentSetting = _experimentSetting;
        timeElapsed = _timeElapsed;
    }

    public final ExperimentSetting experimentSetting;
    public final Double timeElapsed;

}
