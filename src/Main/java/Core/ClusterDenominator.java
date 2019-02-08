package Core;


/**
 * Defines the denominator when deciding if a new pattern belongs to an existing cluster
 *
 * The calculation is |Intersection(Set(Pattern), Set(Cluster))|/X
 * Where X is:
 * MAX_SET: Math.max(|Set(Pattern)|,|Set(Cluster)|)
 * MIN_SET: Math.min(|Set(Pattern)|,|Set(Cluster)|)
 * UNION: |Union(Set(Pattern), Set(Cluster))|
 */
public enum ClusterDenominator {
    MIN_SET,
    MAX_SET,
    UNION

}
