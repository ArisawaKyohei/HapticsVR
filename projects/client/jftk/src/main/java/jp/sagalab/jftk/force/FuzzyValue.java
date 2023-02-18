package jp.sagalab.jftk.force;

import jp.sagalab.jftk.TruthValue;

import java.util.Objects;

/**
 * 一次元のファジィ数を表します。
 */
public class FuzzyValue {

    public static FuzzyValue create(double _v, double _f) {
        return new FuzzyValue(_v, _f);
    }

    public TruthValue includedIn(FuzzyValue _other) {
        double distance = Math.abs(_other.m_vertex - m_vertex);
        double sum = m_fuzziness + _other.m_fuzziness;
        if (Double.isInfinite(sum)) {
            return TruthValue.create(0.0, 1.0);
        } else {
            double nec = Math.max((_other.m_fuzziness - distance) / sum, 0.0);
            double pos = Math.max((sum - distance) / sum, 0.0);
            if (Double.isNaN(nec) && Double.isNaN(pos)) {
                return TruthValue.create(0.5, 1.0);
            } else {
                return TruthValue.create(nec, pos);
            }
        }
    }

    /**
     * 頂点の値を返します。
     * @return 頂点
     */
    public double getVertex() {
        return m_vertex;
    }

    /**
     * ファジネスを返します。
     * @return ファジネス
     */
    public double getFuzziness() {
        return m_fuzziness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuzzyValue that = (FuzzyValue) o;
        return Double.compare(that.m_vertex, m_vertex) == 0 &&
                Double.compare(that.m_fuzziness, m_fuzziness) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_vertex, m_fuzziness);
    }

    @Override
    public String toString() {
        return "FuzzyValue{" +
                "v=" + m_vertex +
                ", f=" + m_fuzziness +
                '}';
    }

    private FuzzyValue(double _vertex, double _fuzziness) {
        this.m_vertex = _vertex;
        this.m_fuzziness = _fuzziness;
    }

    /** 頂点 */
    private final double m_vertex;
    /** ファジネス */
    private final double m_fuzziness;
}
