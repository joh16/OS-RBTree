/**
 * Order statistics Red-Black tree that supports following operations: <br>
 *      Insert - insert an item to tree <br>
 *      Delete - delete an item from tree <br>
 *      Select - select item of the given order <br>
 *      Rank   - get rank of the item <br>
 * <br>
 * @param <T>
 * @author 오지현 (자연과학대학 생명과학부, ID: 2017-16544)
 */
public class OS_RBTree<T extends Comparable<T>>
{
    private final RBTreeNode<T> NIL = new RBTreeNode<>(false, null, null, null, null);
    private int treeSize_ = 0;

    // Constructor
    public OS_RBTree()
    {
        NIL.setLeft(NIL);   // root of empty tree is NIL
        NIL.minusSize();    // to make size 0
    }

    // Accessors
    RBTreeNode<T> root()    { return NIL.left(); } // root is stored at NIL.left_
    public int treeSize()   { return treeSize_;  }

    /**
     * Insert x in tree if x is not in tree
     * @param x item to insert
     * @return x if x is not in tree, otherwise null
     */
    public T insert(T x)
    {
        if (root() == NIL)  // empty tree
        {
            NIL.setLeft(new RBTreeNode<>(false, x, NIL, NIL, NIL)); // new root
            treeSize_++;
            return x;
        }

        RBTreeNode<T> node;
        RBTreeNode<T> child = root();
        boolean isLeft;     // whether child is left_ of node
        do // find x
        {
            node = child;
            int comp = node.item().compareTo(x);
            if (comp != 0) child = node.child(isLeft = (comp > 0));
            else return null; // x found
        }
        while (child != NIL);

        node.setChild(child = new RBTreeNode<>(true, x, node, NIL, NIL), isLeft);

        do node.plusSize(); while ((node = node.parent()) != NIL);      // adjust node size
        if (child.parent().red()) modify_Insert(child);                 // double-red problem
        treeSize_++;
        return x;
    }

    /**
     * Delete x from tree if x is in tree
     * @param x item to delete
     * @return x if x is in tree, otherwise null
     */
    public T delete(T x)
    {
        if (root() == NIL) return null; // empty tree

        RBTreeNode<T> node = root();
        boolean isLeft = true;  // whether child is left_ of node
        do // find x
        {
            int comp = node.item().compareTo(x);
            if (comp != 0) node = node.child(isLeft = (comp > 0));
            else // x found
            {
                if (treeSize_ != 1)
                {
                    RBTreeNode<T> child;                        // store non-NIL child, if exists
                    if (node.right() != NIL)
                    {
                        if (node.left() != NIL)                 // has both left and right non-NIL child
                        {
                            RBTreeNode<T> min = node.right();   // find node with minimum item from right subtree
                            if (isLeft = (min.left() != NIL)) do min = min.left(); while (min.left() != NIL);
                            node.setItem(min.item());           // change item
                            node = min;                         // node to be deleted is min
                        }
                        child = node.right();
                    }
                    else child = node.left();   // upon executing above, node to be deleted has one or no child

                    boolean toModify = !child.red();            // whether modification is needed
                    if (toModify) toModify = !node.red();       // modify iff both deleted node and its child are black
                    else child.setRed(false);
                    child.setParent(node.parent());
                    node.parent().setChild(child, isLeft);
                    while ((node = node.parent()) != NIL) node.minusSize();     // adjust node size
                    if (toModify) modify_Delete(child, isLeft); // lacking-black problem
                    treeSize_--;
                }
                else clear();                   // only (non-NIL) node is root
                return x;
            }
        }
        while (node != NIL);
        return null; // x not found
    }

    /**
     * Select i-th item in tree
     * @param i order, starting from 1
     * @return i-th item if i is equal or less than the size of tree, otherwise null
     */
    public T select(int i)
    {
        if (i > treeSize_) return null; // i is greater than the size of OS_RBTree

        RBTreeNode<T> node = root();
        int comp;                       // if node has the item looking for, comp = 0
        while ((comp = i - (node.left().size() + 1)) != 0)  // rank of node is 1 + left child's size
        {
            if (comp > 0)               // node is less than the item looking for
            {
                i = comp;               // update i
                node = node.right();
            }
            else node = node.left();    // node is greater than the item looking for
        }
        return node.item();
    }

    /**
     * Get rank of x in tree
     * @param x item to get its rank
     * @return rank of x if x is in tree, otherwise 0
     */
    public int rank(T x)
    {
        int r = 0;
        RBTreeNode<T> node = root();
        while (node != NIL)
        {
            int comp = node.item().compareTo(x);
            if (comp > 0) node = node.left();
            else if (comp < 0)          // node is less than x
            {
                r += node.left().size() + 1;            // x is greater than node and all items of left subtree
                node = node.right();
            }
            else return r + node.left().size() + 1;     // x found
        }
        return 0; // x not found
    }

    /**
     * Modify tree to maintain red-black property; called only if there is double-red problem
     * @param node (child) node where double-red problem has occurred
     */
    private void modify_Insert(RBTreeNode<T> node)
    {
        RBTreeNode<T> grandParent = node.parent().parent();
        boolean isLeft = (node.parent() == grandParent.left());         // whether node's parent is left_ of grandParent
        RBTreeNode<T> uncle = grandParent.child(!isLeft);
        if (uncle.red())    // case 1: s is red
        {
            node.parent().setRed(false);
            uncle.setRed(false);
            if (grandParent == root()) return;                          // no more recursion is necessary for root
            grandParent.setRed(true);
            if (grandParent.parent().red()) modify_Insert(grandParent); // deal with grandParent's problem if exists
        }
        else                // case 2: s is black
        {
            if (node == node.parent().child(!isLeft)) rotate(node.parent(), isLeft);    // case 2-1 to 2-2
            rotate(grandParent, !isLeft);                                               // case 2-2: x is left child of p
        }
    }

    /**
     * Modify tree to maintain red-black property; called only if there is lacking-black problem
     * @param node (child) node where lacking-black problem has occurred
     */
    private void modify_Delete(RBTreeNode<T> node, boolean isLeft)
    {
        RBTreeNode<T> parent = node.parent();
        RBTreeNode<T> sibling = parent.child(!isLeft);
        RBTreeNode<T> leftCousin = sibling.child(isLeft);
        RBTreeNode<T> rightCousin = sibling.child(!isLeft);
        int type = (leftCousin.red() ? 0 : 1) + (rightCousin.red() ? 0 : 2);
        if (!parent.red() && type == 3)
        {
            if (sibling.red())      // case 2-4 to 1-*
            {
                rotate(node.parent(), isLeft);
                parent = sibling;
                sibling = leftCousin;
                leftCousin = sibling.child(isLeft);
                rightCousin = sibling.child(!isLeft);
                type = (leftCousin.red() ? 0 : 1) + (rightCousin.red() ? 0 : 2);
            }
            else                    // case 2-1
            {
                sibling.setRed(true);
                if (parent != NIL) modify_Delete(parent, parent == parent.parent().left()); // deal with parent's problem
                return;
            }
        }
        switch (type)
        {
            case 2:                 // case *-3 to *-2
                rotate(sibling, !isLeft);
                rightCousin = sibling.child(!isLeft);
            case 0:
            case 1:                 // case *-2
                rightCousin.setRed(false);
                rotate(parent, isLeft);
                break;
            case 3:                 // case 1-1
                parent.setRed(false);
                sibling.setRed(true);
                break;
        }
    }

    /**
     * Rotate tree maintaining red-black, order statistics, and binary search tree property
     * @param node (parent) node to rotate
     * @param isLeft left-rotate if true, otherwise right-rotate
     */
    private void rotate(RBTreeNode<T> node, boolean isLeft)
    {
        RBTreeNode<T> child = node.child(!isLeft);  // child node to be rotated

        // swap child's item and node's item
        T item = node.item();
        node.setItem(child.item());
        child.setItem(item);

        // modify pointers
        node.setChild(child.child(!isLeft), !isLeft);
        node.child(!isLeft).setParent(node);
        child.setChild(child.child(isLeft), !isLeft);
        child.setChild(node.child(isLeft), isLeft);
        child.child(isLeft).setParent(child);
        node.setChild(child, isLeft);

        // adjust size
        child.updateSize();
    }

    /* rotate method is equivalent to below methods (provided for better understanding)
    public void leftRotate(RBTreeNode<T> node)      // node is P in figure
    {
        RBTreeNode<T> child = node.right();
        T item = node.item();
        node.setItem(child.item());
        child.setItem(item);

        node.setRight(child.right());
        node.right().setParent(node);
        child.setRight(child.left());
        child.setLeft(node.left());
        child.left().setParent(child);
        node.setLeft(child);

        child.updateSize();
    }
    public void rightRotate(RBTreeNode<T> node)     // node is Q in figure
    {
        RBTreeNode<T> child = node.left();
        T item = node.item();
        node.setItem(child.item());
        child.setItem(item);

        node.setLeft(child.left());
        node.left().setParent(node);
        child.setLeft(child.right());
        child.setRight(node.right());
        child.right().setParent(child);
        node.setRight(child);

        child.updateSize();
    }
    */

    @Override
    public String toString() { return (root() == NIL) ? "empty" : printNode(root()); }

    /**
     * Return string that displays non-NIL node as following format: ([R or B] item,(left node),(right node))
     * @param node
     * @return string displaying node
     */
    private String printNode(RBTreeNode<T> node)
    {
        return String.format("(%s %s,%s,%s)", node.red() ? "R" : "B", node.item().toString(),
                             node.left() == NIL ? "NIL" : printNode(node.left()),
                             node.right() == NIL ? "NIL" : printNode(node.right()));
    }

    /**
     * Clear tree;
     */
    public void clear()
    {
        NIL.setLeft(NIL);
        treeSize_ = 0;
    }

    // debug purpose
    /**
     * Return true iff all nodes of tree satisfy order statistics and red-black property
     * @return whether tree is valid
     */
    public boolean isValid() { return blackHeight(root()) != -1; }

    /**
     * Used to check tree properties; if node does not satisfy any property, print it
     * @param node node to get black height
     * @return -1 if node or any of its descendant is invalid, otherwise black height considering node as root
     */
    private int blackHeight(RBTreeNode<T> node)
    {
        if (node == NIL) return 0;
        int leftHeight = blackHeight(node.left());
        int rightHeight = blackHeight(node.right());
        if (leftHeight == -1 || rightHeight == -1 ||
            leftHeight != rightHeight ||                                    // red-black property
            node.size() != node.left().size() + node.right().size() + 1)    // order statistics
        {
            System.out.println(printNode(node));
            return -1;
        }
        else return leftHeight + (node.red() ? 0 : 1);
    }
}

/**
 * Node of Order statistics Red-Black tree.
 * @param <T>
 */
class RBTreeNode<T>
{
    private boolean red_;   // true for red, false for black
    private T item_;
    private RBTreeNode<T> parent_, left_, right_;
    private int size_ = 1;  // size of 'new' node is 1

    // Constructors
    RBTreeNode(boolean red, T item, RBTreeNode<T> parent, RBTreeNode<T> left, RBTreeNode<T> right)
    {
        this.red_ = red;
        this.item_ = item;
        this.parent_ = parent;
        this.left_ = left;
        this.right_ = right;
    }

    // Accessors
    boolean red()                        { return this.red_;      }
    T item()                             { return this.item_;     }
    RBTreeNode<T> parent()               { return this.parent_;   }
    RBTreeNode<T> left()                 { return this.left_;     }
    RBTreeNode<T> right()                { return this.right_;    }
    int size()                           { return this.size_;     }

    // Mutators
    void setRed(boolean red)             { this.red_ = red;       }
    void setItem(T item)                 { this.item_ = item;     }
    void setParent(RBTreeNode<T> parent) { this.parent_ = parent; }
    void setLeft(RBTreeNode<T> left)     { this.left_ = left;     }
    void setRight(RBTreeNode<T> right)   { this.right_ = right;   }

    void plusSize()      { this.size_++; }
    void minusSize()     { this.size_--; }
    void updateSize()    { this.size_ = left_.size_ + right_.size_ + 1; }

    // for convenience
    RBTreeNode<T> child(boolean isLeft)                  { return (isLeft) ? this.left_ : this.right_;     }
    void setChild(RBTreeNode<T> node, boolean isLeft)    { if (isLeft) setLeft(node); else setRight(node); }
}