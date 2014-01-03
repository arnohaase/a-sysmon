package com.ajjpj.asysmon.servlet.performance.bottomup;

import com.ajjpj.asysmon.data.AHierarchicalData;

/**
 * This class serves to identify those 'leaf' nodes that serve as root nodes for bottom-up aggregation, rather than
 *  just using all leaf nodes.<p />
 *
 * This serves several purposes:<ul>
 *     <li>Being selective. You can e.g. pick all JDBC statements, generating a database-centric view of performance.</li>
 *     <li>Limiting detail: You can choose to stop before reaching maximum detail, creating a view of performance
 *          at a level of detail that you want.</li>
 * </ul>
 *
 * @author arno
 */
public interface ABottomUpLeafFilter {
    boolean isLeaf(AHierarchicalData data);
}
