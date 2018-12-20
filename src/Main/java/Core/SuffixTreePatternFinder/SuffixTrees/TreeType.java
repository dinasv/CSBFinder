package Core.SuffixTreePatternFinder.SuffixTrees;

/**
 * Defines the type of the GST
 * VIRTUAL - Saves only one path from the tree at a time. Used when enumerating all possible patterns from the GST.
 * STATIC - The full tree is saved in memory. Used when given an input of possible patterns
 */
public enum TreeType {
    VIRTUAL,
    STATIC;
}
