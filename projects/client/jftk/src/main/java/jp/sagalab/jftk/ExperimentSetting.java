package jp.sagalab.jftk;

import java.nio.file.Path;

public final class ExperimentSetting {

    public ExperimentSetting(
            SnapMethod _snapMethod,
            Double _lowestGridSpansPx,
            Integer _gridCount,
            Integer _gridFactor,
            Integer[] _displayGridFactors,
            Path _backGroundImagePath){
        snapMethod = _snapMethod;
        lowestGridSpanPx = _lowestGridSpansPx;
        gridCount = _gridCount;
        gridFactor = _gridFactor;
        displayedGridFactors = _displayGridFactors;
        backGroundImagePath = _backGroundImagePath;
    }

    public final SnapMethod snapMethod;

    public final Double lowestGridSpanPx;

    public final Integer gridCount;

    public final Integer gridFactor;

    public final Integer[] displayedGridFactors;

    public final Path backGroundImagePath;
}
