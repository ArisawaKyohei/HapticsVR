/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.sagalab.jftk.fragmentation.ellipticalarcs;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 計算時間がO(1)で更新可能な分割点時刻の添字の列を表す.
 * 簡単な不変リスト．
 */
public interface PartitionParameters {
    static PartitionParameters empty() {
        return new Empty();
    }

    static PartitionParameters add(PartitionParameters init, Integer last) {
        return new Cons(init, last);
    }

    List<Integer> getPartitionParameterList();

    Integer size();

    class Empty implements PartitionParameters {
        @Override
        public List<Integer> getPartitionParameterList() {
            return Collections.emptyList();
        }

        @Override
        public Integer size() {
            return 0;
        }
    }

    class Cons implements PartitionParameters {
        private final PartitionParameters init;
        private final Integer last;
        private final Integer size;
        public Cons(PartitionParameters init, Integer last) {
            this.init = init;
            this.last = last;
            this.size = init.size() + 1;
        }

        @Override
        public List<Integer> getPartitionParameterList() {
            List<Integer> result = new LinkedList<>(init.getPartitionParameterList());
            result.add(last);
            return Collections.unmodifiableList(result);
        }

        @Override
        public Integer size() {
            return size;
        }

    }
}