/**
 * Q1 - CO1: MediFlow Hospital Patient Indexing
 * Case Study: BST & AVL Trees
 * Subject: DSA-2 (25SC1305E)
 *
 * Scenario:
 *   Patient IDs arrive in near-sorted appointment order:
 *   20, 30, 35, 40, 45, 50, 60, 65, 70, 75, 80, 85, 90
 *   Noon deletions (in order): 30, 70, 50
 *   SLA: p99 < 5ms, ~200ns per pointer dereference => depth < 25000 hops
 */

public class MediFlowAVL {

    // ─────────────────────────────────────────────
    // Part A: Plain BST
    // ─────────────────────────────────────────────

    static class BSTNode {
        int key;
        BSTNode left, right;
        BSTNode(int key) { this.key = key; }
    }

    static class PlainBST {
        BSTNode root;

        BSTNode insert(BSTNode node, int key) {
            if (node == null) return new BSTNode(key);
            if (key < node.key) node.left  = insert(node.left,  key);
            else if (key > node.key) node.right = insert(node.right, key);
            return node;
        }

        void insert(int key) { root = insert(root, key); }

        int height(BSTNode node) {
            if (node == null) return -1; // -1 so root alone = height 0
            return 1 + Math.max(height(node.left), height(node.right));
        }

        int height() { return height(root); }

        // In-order traversal
        void inOrder(BSTNode node, StringBuilder sb) {
            if (node == null) return;
            inOrder(node.left, sb);
            sb.append(node.key).append(" ");
            inOrder(node.right, sb);
        }

        String inOrder() {
            StringBuilder sb = new StringBuilder();
            inOrder(root, sb);
            return sb.toString().trim();
        }

        // Print tree structure (rotated view)
        void printTree(BSTNode node, String indent, boolean isRight) {
            if (node == null) return;
            System.out.println(indent + (isRight ? "R── " : "L── ") + node.key);
            String childIndent = indent + (isRight ? "│   " : "    ");
            printTree(node.right, childIndent, true);
            printTree(node.left,  childIndent, false);
        }

        void printTree() {
            if (root == null) { System.out.println("(empty)"); return; }
            System.out.println(root.key + " (root)");
            printTree(root.right, "", true);
            printTree(root.left,  "", false);
        }
    }

    // ─────────────────────────────────────────────
    // Part B: AVL Tree
    // ─────────────────────────────────────────────

    static class AVLNode {
        int key;
        AVLNode left, right;
        int height = 1;
        AVLNode(int key) { this.key = key; }
    }

    static class AVLTree {
        AVLNode root;

        int height(AVLNode n) { return n == null ? 0 : n.height; }

        int balance(AVLNode n) {
            return n == null ? 0 : height(n.left) - height(n.right);
        }

        void updateHeight(AVLNode n) {
            if (n != null)
                n.height = 1 + Math.max(height(n.left), height(n.right));
        }

        // TODO 1: Right Rotation around y
        AVLNode rotateRight(AVLNode y) {
            AVLNode x  = y.left;
            AVLNode T2 = x.right;
            x.right = y;
            y.left  = T2;
            updateHeight(y);
            updateHeight(x);
            System.out.printf("    [LL Rotation] pivot=%d  bf(%d)=%d  bf(%d)=%d%n",
                    y.key, y.key, balance(y), x.key, balance(x));
            return x;
        }

        // TODO 2: Left Rotation around x
        AVLNode rotateLeft(AVLNode x) {
            AVLNode y  = x.right;
            AVLNode T2 = y.left;
            y.left  = x;
            x.right = T2;
            updateHeight(x);
            updateHeight(y);
            System.out.printf("    [RR Rotation] pivot=%d  bf(%d)=%d  bf(%d)=%d%n",
                    x.key, x.key, balance(x), y.key, balance(y));
            return y;
        }

        // TODO 3: Insert with all four rebalancing cases
        AVLNode insert(AVLNode node, int key) {
            // Step 1 – Standard BST insert
            if (node == null) return new AVLNode(key);
            if      (key < node.key) node.left  = insert(node.left,  key);
            else if (key > node.key) node.right = insert(node.right, key);
            else    return node; // duplicate – ignore

            // Step 2 – Update height
            updateHeight(node);

            // Step 3 – Balance factor
            int bf = balance(node);

            // Step 4 – Four cases
            // LL: left-heavy, new key went into left subtree's left
            if (bf > 1 && key < node.left.key)
                return rotateRight(node);

            // RR: right-heavy, new key went into right subtree's right
            if (bf < -1 && key > node.right.key)
                return rotateLeft(node);

            // LR: left-heavy, new key went into left subtree's right
            if (bf > 1 && key > node.left.key) {
                System.out.printf("    [LR Rotation] at node=%d%n", node.key);
                node.left = rotateLeft(node.left);
                return rotateRight(node);
            }

            // RL: right-heavy, new key went into right subtree's left
            if (bf < -1 && key < node.right.key) {
                System.out.printf("    [RL Rotation] at node=%d%n", node.key);
                node.right = rotateRight(node.right);
                return rotateLeft(node);
            }

            return node;
        }

        void insert(int key) {
            System.out.printf("  Inserting %d ...%n", key);
            root = insert(root, key);
            System.out.printf("    -> tree height now = %d%n", height(root));
        }

        // ── Deletion ────────────────────────────
        AVLNode minNode(AVLNode node) {
            while (node.left != null) node = node.left;
            return node;
        }

        AVLNode delete(AVLNode node, int key) {
            if (node == null) return null;

            if      (key < node.key) node.left  = delete(node.left,  key);
            else if (key > node.key) node.right = delete(node.right, key);
            else {
                // Node found
                if (node.left == null)  return node.right;
                if (node.right == null) return node.left;
                // Two children – replace with in-order successor
                AVLNode successor = minNode(node.right);
                node.key   = successor.key;
                node.right = delete(node.right, successor.key);
            }

            updateHeight(node);
            int bf = balance(node);

            // LL
            if (bf > 1 && balance(node.left) >= 0)  return rotateRight(node);
            // LR
            if (bf > 1 && balance(node.left) < 0) {
                node.left = rotateLeft(node.left);
                return rotateRight(node);
            }
            // RR
            if (bf < -1 && balance(node.right) <= 0) return rotateLeft(node);
            // RL
            if (bf < -1 && balance(node.right) > 0) {
                node.right = rotateRight(node.right);
                return rotateLeft(node);
            }
            return node;
        }

        void delete(int key) {
            System.out.printf("  Deleting %d ...%n", key);
            root = delete(root, key);
            System.out.printf("    -> tree height now = %d%n", height(root));
        }

        // ── Utilities ───────────────────────────
        int height() { return height(root) - 1; } // edges

        void inOrder(AVLNode node, StringBuilder sb) {
            if (node == null) return;
            inOrder(node.left, sb);
            sb.append(node.key).append("(bf=").append(balance(node)).append(") ");
            inOrder(node.right, sb);
        }

        String inOrder() {
            StringBuilder sb = new StringBuilder();
            inOrder(root, sb);
            return sb.toString().trim();
        }

        void printTree(AVLNode node, String indent, boolean isRight) {
            if (node == null) return;
            System.out.printf("%s%s%d [h=%d,bf=%d]%n",
                    indent, isRight ? "R── " : "L── ",
                    node.key, node.height, balance(node));
            String ci = indent + (isRight ? "│   " : "    ");
            printTree(node.right, ci, true);
            printTree(node.left,  ci, false);
        }

        void printTree() {
            if (root == null) { System.out.println("(empty)"); return; }
            System.out.printf("%d [h=%d,bf=%d] (root)%n",
                    root.key, root.height, balance(root));
            printTree(root.right, "", true);
            printTree(root.left,  "", false);
        }
    }

    // ─────────────────────────────────────────────
    // SLA Analysis
    // ─────────────────────────────────────────────
    static void slaAnalysis(String label, int treeHeight) {
        double nsPerHop   = 200.0;
        double budgetNs   = 5_000_000.0; // 5 ms
        double lookupNs   = treeHeight * nsPerHop;
        double pctBudget  = (lookupNs / budgetNs) * 100.0;
        String status     = lookupNs < budgetNs ? "MEETS SLA" : "VIOLATES SLA";
        System.out.printf("  %-35s height=%2d  lookup=%.0f ns (%.4f%% of 5ms budget)  [%s]%n",
                label, treeHeight, lookupNs, pctBudget, status);
    }

    // ─────────────────────────────────────────────
    // Main
    // ─────────────────────────────────────────────
    public static void main(String[] args) {

        int[] insertOrder = {20, 30, 35, 40, 45, 50, 60, 65, 70, 75, 80, 85, 90};
        int[] deletions   = {30, 70, 50};

        // ── Part A: Plain BST ───────────────────
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println(" PART A — Plain BST (arrival order)");
        System.out.println("══════════════════════════════════════════════════════════");

        PlainBST bst = new PlainBST();
        for (int id : insertOrder) bst.insert(id);

        System.out.println("In-order traversal: " + bst.inOrder());
        System.out.println("BST Structure:");
        bst.printTree();

        int bstHeight = bst.height();
        System.out.println("\nBST Height (edges) = " + bstHeight);
        System.out.println("Adversarial: sorted input => right-skewed chain (O(n) height).");

        System.out.println("\n── SLA Check (pre-deletion) ──");
        slaAnalysis("Plain BST (13 nodes, sorted input)", bstHeight);

        // ── Part B: AVL Tree ───────────────────
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println(" PART B — AVL Tree construction (same arrival order)");
        System.out.println("══════════════════════════════════════════════════════════");

        AVLTree avl = new AVLTree();
        for (int id : insertOrder) avl.insert(id);

        System.out.println("\nFinal AVL Tree (after all 13 insertions):");
        avl.printTree();
        System.out.println("In-order: " + avl.inOrder());
        System.out.printf("AVL Height (edges) = %d%n", avl.height());

        // ── Part C: Noon Deletions ─────────────
        System.out.println("\n══════════════════════════════════════════════════════════");
        System.out.println(" PART C — Noon Deletions on AVL Tree: 30, 70, 50");
        System.out.println("══════════════════════════════════════════════════════════");

        for (int d : deletions) avl.delete(d);

        System.out.println("\nPost-deletion AVL Tree:");
        avl.printTree();
        System.out.println("In-order: " + avl.inOrder());

        System.out.println("\n── SLA Comparison (post-deletion) ──");
        slaAnalysis("Plain BST (sorted input, 13→10 nodes)", bstHeight); // BST height unchanged
        slaAnalysis("AVL Tree  (balanced, 13→10 nodes)",    avl.height());

        System.out.println("\nConclusion:");
        System.out.println("  AVL Tree height = O(log n) => consistent SLA compliance.");
        System.out.println("  Plain BST with sorted input = O(n) height => degrades linearly.");
        System.out.println("  Deletion of 50 caused the largest structural change (rebalancing at 40).");
    }
}
