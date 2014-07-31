/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.operation;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.session.history.change.Change;
import com.sk89q.worldedit.session.history.changeset.ChangeSet;
import com.sk89q.worldedit.session.history.UndoContext;
import com.sk89q.worldedit.util.task.progress.Progress;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Performs an undo or redo from a given {@link ChangeSet}.
 */
public class ChangeSetExecutor extends AbstractOperation {

    public enum Type {UNDO, REDO}

    private final Iterator<Change> iterator;
    private final Type type;
    private final UndoContext context;

    /**
     * Create a new instance.
     *
     * @param changeSet the change set
     * @param type type of change
     * @param context the undo context
     */
    private ChangeSetExecutor(ChangeSet changeSet, Type type, UndoContext context) {
        checkNotNull(changeSet);
        checkNotNull(type);
        checkNotNull(context);

        this.type = type;
        this.context = context;

        if (type == Type.UNDO) {
            iterator = changeSet.backwardIterator();
        } else {
            iterator = changeSet.forwardIterator();
        }
    }

    @Override
    public Result resume(RunContext run) throws WorldEditException {
        if (run.isCancelled()) {
            return Result.STOP;
        }

        while (iterator.hasNext()) {
            Change change = iterator.next();
            if (type == Type.UNDO) {
                change.undo(context);
            } else {
                change.redo(context);
            }

            if (!run.shouldContinue()) {
                return Result.CONTINUE;
            }
        }

        return Result.STOP;
    }

    @Override
    public Progress getProgress() {
        return Progress.indeterminate();
    }

    /**
     * Create a new undo operation.
     *
     * @param changeSet the change set
     * @param context an undo context
     * @return an operation
     */
    public static ChangeSetExecutor createUndo(ChangeSet changeSet, UndoContext context) {
        return new ChangeSetExecutor(changeSet, Type.UNDO, context);
    }

    /**
     * Create a new redo operation.
     *
     * @param changeSet the change set
     * @param context an undo context
     * @return an operation
     */
    public static ChangeSetExecutor createRedo(ChangeSet changeSet, UndoContext context) {
        return new ChangeSetExecutor(changeSet, Type.REDO, context);
    }

}
