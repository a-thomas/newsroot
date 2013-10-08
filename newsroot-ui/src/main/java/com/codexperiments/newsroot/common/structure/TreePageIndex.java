package com.codexperiments.newsroot.common.structure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codexperiments.newsroot.common.Page;

/**
 * PageIndex uses a Red-Black tree to store pages. It provides O(log(n)) complexity on retrieval and insertion operations.
 */
public class TreePageIndex<TItem> implements PageIndex<TItem> {
    private static final boolean COLOR_BLACK = false;
    private static final boolean COLOR_RED = true;

    private final Node NIL = new Node();
    private Node mRoot;

    public TreePageIndex() {
        super();
        mRoot = NIL;
    }

    public List<TItem> find(int pIntervalIndex, int pIntervalSize) {
        if (pIntervalIndex < 0) throw new IllegalArgumentException();

        Node lX = mRoot;
        Node lY = mRoot;
        Node lFoundNode = NIL;
        int lFoundStartIndex = 0;
        int lCurrentStartIndex = 0;
        int lCurrentIndex = 0;

        while (lX != NIL) {
            lCurrentIndex = lCurrentStartIndex + lX.mStartIndex;
            // Current node is greater than the searched index. Ignore it and go left where there will be lower nodes.
            if (lCurrentIndex > pIntervalIndex) {
                lY = lX;
                lX = lX.mLeft;
            }
            // Current node is lower than or equal to the search index. Remember it and go right if it is not yet the right node.
            else {
                lFoundNode = lX;
                lFoundStartIndex = lCurrentIndex;
                lCurrentStartIndex = lFoundStartIndex + lFoundNode.size();
                if (pIntervalIndex >= lCurrentStartIndex) lX = lX.mRight;
                else lX = NIL;
            }
        }
        // If the elements of the first page are requested, the search algorithm above will always descend the tree to the left
        // and no node will be memorized. the lCurrentNode will remains NIL. In that case, the last node seen, which is also the
        // lowest one in the tree, is the right one.
        if (lFoundNode == NIL) lFoundNode = lY;

        // Save elements in the found interval.
        return window(lFoundNode, pIntervalSize, pIntervalIndex - lFoundStartIndex);
    }

    private List<TItem> window(Node pNode, int pIntervalSize, int lStartIndex) {
        List<TItem> lItems = new ArrayList<TItem>(pIntervalSize);

        Node lX = pNode;
        // Iterate through nodes in-order.
        while ((pIntervalSize > 0) && (lX != NIL)) {
            Page<? extends TItem> lPage = lX.mPage;
            int lPageSize = lX.size();
            // Copy all node items until we get expected item count or until there is no more item in the node.
            while ((pIntervalSize > 0) && (lStartIndex < lPageSize)) {
                lItems.add(lPage.get(lStartIndex));
                ++lStartIndex;
                --pIntervalSize;
            }
            // Go to next node.
            lX = lX.successor();
            lStartIndex = 0;
        }
        return lItems;
    }

    public void insert(Page<? extends TItem> pPage) {
        long lKey = pPage.lowerBound();
        Node lX = mRoot;
        Node lY = NIL;

        // Finds insertion node, at the bottom of the tree.
        while (lX != NIL) {
            lY = lX;

            boolean lGoLeft = (lKey < lX.mKey);
            if (lGoLeft) lX = lX.mLeft;
            else lX = lX.mRight;
            maintainOnInsertTraversal(lY, lGoLeft, pPage);
        }

        // Insert Z node into the tree.
        Node pZ = new Node(lKey, COLOR_RED, lY, pPage);
        // Rebalance the tree after insertion.
        insertFixup(pZ);
    }

    /**
     * Rebalance the tree after insertion.
     * 
     * @param pZ Inserted node.
     */
    private void insertFixup(Node pZ) {
        Node lZ = pZ;
        Node lZParent = lZ.mParent;
        Node lZGranParent = lZParent.mParent;
        // (lZ != NIL) && (lZ != mRoot)
        while (lZParent.mColor == COLOR_RED) {
            if (lZParent == lZGranParent.mLeft) {
                Node lY = lZGranParent.mRight;
                if (lY.mColor == COLOR_RED) {
                    lZParent.mColor = COLOR_BLACK;
                    lY.mColor = COLOR_BLACK;
                    lZGranParent.mColor = COLOR_RED;

                    lZ = lZGranParent;
                    lZParent = lZ.mParent;
                    lZGranParent = lZParent.mParent;
                } else {
                    if (lZ == lZParent.mRight) {
                        lZ = lZParent;
                        leftRotate(lZ);
                        lZParent = lZ.mParent;
                        lZGranParent = lZParent.mParent;
                    }
                    lZParent.mColor = COLOR_BLACK;
                    lZGranParent.mColor = COLOR_RED;
                    rightRotate(lZGranParent);
                    lZParent = lZ.mParent;
                    lZGranParent = lZParent.mParent;
                }
            }
            // Same clause with right and left exchanged.
            else {
                Node lY = lZGranParent.mLeft;
                if (lY.mColor == COLOR_RED) {
                    lZParent.mColor = COLOR_BLACK;
                    lY.mColor = COLOR_BLACK;
                    lZGranParent.mColor = COLOR_RED;

                    lZ = lZGranParent;
                    lZParent = lZ.mParent;
                    lZGranParent = lZParent.mParent;
                } else {
                    if (lZ == lZParent.mLeft) {
                        lZ = lZParent;
                        rightRotate(lZ);
                        lZParent = lZ.mParent;
                        lZGranParent = lZParent.mParent;
                    }
                    lZParent.mColor = COLOR_BLACK;
                    lZGranParent.mColor = COLOR_RED;
                    leftRotate(lZGranParent);
                    lZParent = lZ.mParent;
                    lZGranParent = lZParent.mParent;
                }
            }
        }
        mRoot.mColor = COLOR_BLACK;
    }

    /**
     * Rotate two nodes while still maintaining tree invariants to help balancing the tree.
     * 
     * <pre>
     *          Root                    Root
     *            |                      |
     *          +---+                  +---+
     *          | Y |                  | X |
     *          +---+                  +---+
     *         /    \       /         /     \
     *      +---+     C    /======   A     +---+
     *      | X |         /      /         | Y |
     *      +---+         \      \         +---+
     *     /     \         \======        /     \
     *    A       B         \            B       C
     * </pre>
     */
    private void leftRotate(Node pX) {
        Node lY = pX.mRight;
        Node lB = lY.mLeft;
        Node lRoot = pX.mParent;

        // Turn Y's left sub-tree into X's right sub-tree.
        pX.mRight = lB;
        if (lB != NIL) {
            lB.mParent = pX;
            // X's right child was Y and is now B. Thus X's successor link doesn't changes as it was already NIL before.
        } else {
            // X's right child was Y and is now NIL. Thus Y becomes its new in-order successor.
            pX.mSuccessor = lY;
        }

        // Y becomes parent.
        pX.mParent = lY;
        lY.mParent = lRoot;
        // Update former X's parent.
        if (lRoot == NIL) mRoot = lY; // X was the root.
        else if (pX == lRoot.mLeft) lRoot.mLeft = lY; // X was the left sub-tree.
        else lRoot.mRight = lY;// X was the left sub-tree.
        // Associates X and Y
        lY.mLeft = pX;
        pX.mParent = lY;

        maintainOnLeftRotate(pX, lY);
    }

    /**
     * Reverse operation of leftRotate().
     * 
     * <pre>
     *          Root                    Root
     *            |                      |
     *          +---+                  +---+
     *          | Y |                  | X |
     *          +---+                  +---+
     *         /     \         \      /     \
     *      +---+     C   ======\    A     +---+
     *      | X |         \      \         | Y |
     *      +---+         /      /         +---+
     *     /     \        ======/         /     \
     *    A       B            /         B       C
     * </pre>
     */
    private void rightRotate(Node pY) {
        Node lX = pY.mLeft;
        Node lB = lX.mRight;
        Node lParent = pY.mParent;

        // Turn Y's left sub-tree into X's right sub-tree.
        pY.mLeft = lB;
        if (lB != NIL) lB.mParent = pY;
        // X's right child was B (which might be nil) and is now Y (which is non-nil). Its in-order successor can thus be removed.
        lX.mSuccessor = NIL;

        // X becomes parent.
        pY.mParent = lX;
        lX.mParent = lParent;
        // Update former X's parent.
        if (lParent == NIL) mRoot = lX; // Y was the root.
        else if (pY == lParent.mRight) lParent.mRight = lX; // Y was the right sub-tree.
        else lParent.mLeft = lX;// Y was the left sub-tree.
        // Associates X and Y
        lX.mRight = pY;
        pY.mParent = lX;

        maintainOnRightRotate(pY, lX);
    }

    protected void maintainOnInsertTraversal(Node pX, boolean pLeft, Page<? extends TItem> pPage) {
        if (pLeft) {
            pX.mStartIndex += pPage.size();
        }
    }

    protected void maintainOnLeftRotate(Node pX, Node pY) {
        // Y depended on B only, now it depends on X too (see leftRotate() diagram).
        pY.mStartIndex += pX.mStartIndex + pX.size();
    }

    protected void maintainOnRightRotate(Node pY, Node pX) {
        // Y depended on X + B, now it depends on B only (see rightRotate() diagram).
        pY.mStartIndex -= pX.mStartIndex + pX.size();
    }

    public int size() {
        int lSize = 0;
        Node lX = mRoot;
        while (lX != NIL) {
            lSize += lX.mStartIndex + lX.size();
            lX = lX.mRight;
        }
        return lSize;
    }

    @Override
    public String toString() {
        StringBuilder lBuilder = new StringBuilder();

        Deque<Node> lDeque = new ArrayDeque<Node>();
        Map<Node, Integer> lLevels = new HashMap<Node, Integer>();
        Node lNode = mRoot;
        int lOldLevel = 0;
        int lLevel = 0;
        lDeque.offer(lNode);
        lLevels.put(lNode, lLevel);

        // Perform a breadth-first traversal.
        while (!lDeque.isEmpty()) {
            lNode = lDeque.pop();
            lOldLevel = lLevel;
            lLevel = lLevels.get(lNode);
            if (lOldLevel != lLevel) {
                lBuilder.append("\n");
                lOldLevel = lLevel;
            }

            lBuilder.append(lNode.toString());
            lBuilder.append(lNode.nodeCount());
            lBuilder.append(" ");
            if (lNode.mLeft != NIL) {
                lDeque.offer(lNode.mLeft);
                lLevels.put(lNode.mLeft, lLevel + 1);
            }
            if (lNode.mRight != NIL) {
                lDeque.offer(lNode.mRight);
                lLevels.put(lNode.mRight, lLevel + 1);
            }
        }
        return lBuilder.toString();
    }

    /**
     * <ol>
     * <li>1. Every node is either red or black.</li>
     * <li>2. The root is black.</li>
     * <li>3. Every leaf (NIL) is black.</li>
     * <li>4. If a node is red, then both its children are black.</li>
     * <li>5. For each node, all simple paths from the node to descendant leaves contain the same number of black nodes.</li>
     * </ol>
     */
    private class Node {
        private boolean mColor;
        private long mKey;
        private int mStartIndex;
        private Page<? extends TItem> mPage;

        private Node mParent;
        private Node mLeft;
        private Node mRight;
        private Node mSuccessor;

        protected Node() {
            mColor = COLOR_BLACK;
            mKey = -1;
            mStartIndex = 0;
            mPage = null;

            mParent = this;
            mLeft = this;
            mRight = this;
            mSuccessor = this;
        }

        protected Node(long pKey, boolean pColor, Node pParent, Page<? extends TItem> pPage) {
            super();
            mColor = pColor;
            mKey = pKey;
            mStartIndex = 0; // XXX pPage.size();
            mPage = pPage;

            mParent = pParent;
            mLeft = NIL;
            mRight = NIL;

            if (pParent == NIL) {
                mRoot = this;
                mSuccessor = NIL;
            } else if (mKey < pParent.mKey) {
                pParent.mLeft = this;
                // In-order successor is the parent if created node is on the left
                mSuccessor = pParent;
            } else if (mKey > pParent.mKey) {
                pParent.mRight = this;
                // In-order successor is the parent's former successor if current node is on the right (as it becomes the new
                // parent's in-order successor).
                mSuccessor = pParent.mSuccessor;
                pParent.mSuccessor = NIL;
            } else throw new IllegalArgumentException();
        }

        private Node successor() {
            if (mSuccessor != NIL) {
                return mSuccessor;
            } else {
                Node lX = mRight;
                while (lX.mLeft != NIL) {
                    lX = lX.mLeft;
                }
                return lX;
            }
        }

        protected int size() {
            return mPage.size();
        }

        protected int nodeCount() {
            int lCount = 0;
            if (mLeft != NIL) ++lCount;
            if (mRight != NIL) ++lCount;
            return lCount;
        }

        @Override
        public String toString() {
            String lColor = (mColor == COLOR_BLACK) ? "B" : "R";
            String lDirection = (mParent.mLeft == this) ? "L" : ((mParent.mRight == this) ? "R" : "-");
            int lCount = (mLeft != NIL) ? ((mRight != NIL) ? 2 : 1) : ((mRight != NIL) ? 1 : 0);
            if (this == NIL) return "NIL";
            else return "[" + lCount + "," + size() + "," + lDirection + "," + lColor + "," + mKey + "," + mStartIndex + ",->"
                            + mSuccessor.mKey + "]";
        }
    }
}
