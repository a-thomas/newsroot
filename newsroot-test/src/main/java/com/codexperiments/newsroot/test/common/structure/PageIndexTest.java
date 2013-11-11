package com.codexperiments.newsroot.test.common.structure;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import android.util.Log;

import com.codexperiments.newsroot.common.Page;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.common.structure.TreePageIndex;

public class PageIndexTest extends TestCase {
    // public final void testSomething() {
    // PageIndex<Object> lPageIndex = new TreePageIndex<Object>();
    // // for (int i = 1; i < 12; ++i) {
    // for (int i = 11; i > 0; --i) {
    // lPageIndex.insert(new MyPage(i, i, i + 10));
    // Log.e("", "");
    // Log.e("", "*****************************");
    // Log.e("", lPageIndex.toString());
    // }
    //
    // Log.e("", "*****************************");
    // int sum = 0;
    // for (int i = 1; i < 12; ++i) {
    // sum += i + 10;
    // Log.e("", sum + " ");
    // }
    // }
    //

    private static final MyPage PAGE_1 = new MyPage(10, 14, 5);
    // This page set represent the following values: [-10, -9, -6, -5, -4, -3, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 14, 15, 16,
    // 17, 20, 21, 22, 23, 24, 25, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40].
    private static final List<MyPage> PAGE_SET_1 = new ArrayList<MyPage>();

    static {
        PAGE_SET_1.add(new MyPage(15, 20, 4));
        PAGE_SET_1.add(new MyPage(21, 30, 6));
        PAGE_SET_1.add(new MyPage(0, 4, 5));
        PAGE_SET_1.add(new MyPage(5, 9, 5));
        PAGE_SET_1.add(new MyPage(10, 10, 1));
        PAGE_SET_1.add(new MyPage(-10, -6, 3));
        PAGE_SET_1.add(new MyPage(-5, -3, 3));
        PAGE_SET_1.add(new MyPage(-2, -1, 0));
        PAGE_SET_1.add(new MyPage(11, 14, 2));
        PAGE_SET_1.add(new MyPage(31, 40, 10));
    }

    public void testInsert() {
        PageIndex<Integer> lIndex = new TreePageIndex<Integer>();
        List<Integer> lValues = new ArrayList<Integer>();
        int lSize = 0;
        assertThat(lIndex.size(), equalTo(lSize));

        // Insert a new page during each iteration.
        for (MyPage lPage : PAGE_SET_1) {
            lSize += lPage.size();
            lIndex.insert(lPage);
            lValues = addValuesSorted(lValues, lPage);

            // Check each element of the new page has been inserted among others and are returned in order.
            assertThat(lIndex.size(), equalTo(lSize));
            List<Integer> lInterval = lIndex.find(0, lSize);
            for (int i = 0; i < lSize; ++i) {
                assertThat(lInterval.get(i), equalTo(lValues.get(i)));
            }

            Log.d("testInsert", "Current tree:");
            Log.d("testInsert", lIndex.toString());
            Log.d("testInsert", "Inserted values: " + lValues.toString());
            Log.d("testInsert", "Result interval: " + lInterval.toString());
        }
    }

    public void testFind_zeroElements_emptyIndex() {
        PageIndex<Integer> lIndex = new TreePageIndex<Integer>();
        List<Integer> lInterval = lIndex.find(0, 0);
        assertThat(lInterval, hasSize(0));
    }

    public void testFind_zeroElements_singlePageIndex() {
        PageIndex<Integer> lIndex = new TreePageIndex<Integer>();
        lIndex.insert(PAGE_1);
        List<Integer> lInterval = lIndex.find(0, 0);
        assertThat(lInterval, hasSize(0));
    }

    public void testFind_zeroElements_multiPageIndex() {
        PageIndex<Integer> lIndex = makeIndex(PAGE_SET_1);
        List<Integer> lExpectedValues = sortedValues(PAGE_SET_1);

        for (int i = 0; i < lExpectedValues.size(); ++i) {
            List<Integer> lInterval = lIndex.find(i, 0);
            assertThat(lInterval, hasSize(0));
        }
        List<Integer> lIntervalAfter = lIndex.find(lExpectedValues.size(), 0);
        assertThat(lIntervalAfter, hasSize(0));
    }

    public void testFind_oneElement_emptyIndex() {
        PageIndex<Integer> lIndex = new TreePageIndex<Integer>();
        List<Integer> lInterval1 = lIndex.find(0, 1);
        assertThat(lInterval1, hasSize(0));

        List<Integer> lInterval2 = lIndex.find(1, 1);
        assertThat(lInterval2, hasSize(0));
    }

    public void testFind_oneElement_singlePageIndex() {
        PageIndex<Integer> lIndex = new TreePageIndex<Integer>();
        lIndex.insert(new MyPage(10, 14, 5));
        List<Integer> lExpectedValues = sortedValues(PAGE_1);

        for (int i = 0; i < lExpectedValues.size(); ++i) {
            List<Integer> lInterval = lIndex.find(i, 1);
            assertThat(lInterval, hasSize(1));
            assertThat(lInterval.get(0), equalTo(lExpectedValues.get(i)));
        }
        List<Integer> lIntervalAfter = lIndex.find(lExpectedValues.size(), 1);
        assertThat(lIntervalAfter, hasSize(0));
    }

    public void testFind_oneElement_multiPageIndex() {
        PageIndex<Integer> lIndex = makeIndex(PAGE_SET_1);
        List<Integer> lExpectedValues = sortedValues(PAGE_SET_1);

        for (int i = 0; i < lExpectedValues.size(); ++i) {
            List<Integer> lInterval = lIndex.find(i, 1);
            assertThat(lInterval, hasSize(1));
            assertThat(lInterval.get(0), equalTo(lExpectedValues.get(i)));
        }
        List<Integer> lIntervalAfter = lIndex.find(lExpectedValues.size(), 1);
        assertThat(lIntervalAfter, hasSize(0));
    }

    public void testFind_severalElements_emptyIndex() {
        PageIndex<Integer> lIndex = new TreePageIndex<Integer>();
        List<Integer> lInterval1 = lIndex.find(0, 5);
        assertThat(lInterval1, hasSize(0));

        List<Integer> lInterval2 = lIndex.find(1, 5);
        assertThat(lInterval2, hasSize(0));
    }

    public void testFind_severalElements_singlePageIndex() {
        PageIndex<Integer> lIndex = new TreePageIndex<Integer>();
        lIndex.insert(new MyPage(10, 14, 5));
        List<Integer> lExpectedValues = sortedValues(PAGE_1);

        List<Integer> lInterval1 = lIndex.find(0, lExpectedValues.size() - 1);
        assertThat(lInterval1, hasSize(lExpectedValues.size() - 1));
        assertThat(lInterval1, equalTo(lExpectedValues.subList(0, lExpectedValues.size() - 1)));

        List<Integer> lInterval2 = lIndex.find(0, lExpectedValues.size() + 1);
        assertThat(lInterval2, hasSize(lExpectedValues.size()));
        assertThat(lInterval2, equalTo(lExpectedValues));

        List<Integer> lIntervalAfter = lIndex.find(lExpectedValues.size(), 1);
        assertThat(lIntervalAfter, hasSize(0));
    }

    public void testFind_severalElements_multiPageIndex() {
        PageIndex<Integer> lIndex = makeIndex(PAGE_SET_1);

        List<Integer> lIntervalBegin1 = lIndex.find(0, 3);
        assertThat(lIntervalBegin1, hasSize(3));
        assertThat(lIntervalBegin1, equalTo(sortedValues(-10, -9, -6)));

        List<Integer> lIntervalBegin2 = lIndex.find(0, 4);
        assertThat(lIntervalBegin2, hasSize(4));
        assertThat(lIntervalBegin2, equalTo(sortedValues(-10, -9, -6, -5)));

        List<Integer> lIntervalBegin3 = lIndex.find(0, 5);
        assertThat(lIntervalBegin3, hasSize(5));
        assertThat(lIntervalBegin3, equalTo(sortedValues(-10, -9, -6, -5, -4)));

        List<Integer> lIntervalInside1 = lIndex.find(6, 5);
        assertThat(lIntervalInside1, hasSize(5));
        assertThat(lIntervalInside1, equalTo(sortedValues(0, 1, 2, 3, 4)));

        List<Integer> lIntervalInside2 = lIndex.find(6, 6);
        assertThat(lIntervalInside2, hasSize(6));
        assertThat(lIntervalInside2, equalTo(sortedValues(0, 1, 2, 3, 4, 5)));

        List<Integer> lIntervalInside3 = lIndex.find(7, 6);
        assertThat(lIntervalInside3, hasSize(6));
        assertThat(lIntervalInside3, equalTo(sortedValues(1, 2, 3, 4, 5, 6)));

        List<Integer> lIntervalEnd2 = lIndex.find(lIndex.size() - 1, 5);
        assertThat(lIntervalEnd2, hasSize(1));
        assertThat(lIntervalEnd2.get(0), equalTo(40));

        List<Integer> lIntervalAfter = lIndex.find(lIndex.size(), 5);
        assertThat(lIntervalAfter, hasSize(0));
    }

    public void testFind_invalidIndex() {
        try {
            PageIndex<Integer> lIndex = makeIndex(PAGE_SET_1);
            lIndex.find(-666, 0);
            fail();
        } catch (IllegalArgumentException eIllegalArgumentException) {
        }
    }

    private PageIndex<Integer> makeIndex(List<MyPage> pPageSet) {
        PageIndex<Integer> lIndex = new TreePageIndex<Integer>();
        for (MyPage lPage : pPageSet) {
            lIndex.insert(lPage);
            Log.d("", "***************************************");
            Log.d("", lIndex.toString());
        }
        return lIndex;
    }

    private List<Integer> sortedValues(MyPage pPage) {
        List<MyPage> lPages = new ArrayList<MyPage>();
        lPages.add(pPage);
        return sortedValues(lPages);
    }

    private List<Integer> sortedValues(List<MyPage> pPageSet) {
        List<Integer> lValues = new ArrayList<Integer>();
        for (MyPage lPage : pPageSet) {
            for (Integer lValue : lPage) {
                lValues.add(lValue);
            }
        }
        Collections.sort(lValues);
        return lValues;
    }

    private List<Integer> sortedValues(int... pValues) {
        List<Integer> lResult = new ArrayList<Integer>();
        for (int lValue : pValues) {
            lResult.add(lValue);
        }
        Collections.sort(lResult);
        return lResult;
    }

    private List<Integer> addValuesSorted(List<Integer> pValues, MyPage pPage) {
        List<Integer> lResult = new ArrayList<Integer>(pValues);
        for (Integer lItem : pPage) {
            lResult.add(lItem);
        }
        Collections.sort(lResult);
        return lResult;
    }

    private static class MyPage implements Page<Integer>, Iterable<Integer> {
        private long mLowerBound;
        private long mUpperBound;
        private int mSize;
        private List<Integer> mItems;

        public MyPage(int pLowerBound, int pUpperBound, int pSize) {
            if ((pUpperBound - pLowerBound) < pSize - 1) throw new IllegalArgumentException();

            mLowerBound = pLowerBound;
            mUpperBound = pUpperBound;
            mSize = pSize;
            mItems = new ArrayList<Integer>(pSize);
            if (pSize > 0) {
                for (int i = 0; i < pSize - 1; ++i) {
                    mItems.add(Integer.valueOf((int) mLowerBound + i));
                }
                mItems.add(Integer.valueOf((int) mUpperBound));
            }
        }

        public long lowerBound() {
            return mLowerBound;
        }

        public long upperBound() {
            return mUpperBound;
        }

        public int size() {
            return mSize;
        }

        public Integer get(int pIndex) {
            return mItems.get(pIndex);
        }

        @Override
        public Iterator<Integer> iterator() {
            return mItems.iterator();
        }
    }
}
