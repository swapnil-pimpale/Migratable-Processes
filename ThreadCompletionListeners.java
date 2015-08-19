/**
 * ThreadCompletionListeners: Interface to listen for thread completions
 */
package com.company;

public interface ThreadCompletionListeners {
        void notifyOfThreadCompletion(MigratableProcess process);
}
