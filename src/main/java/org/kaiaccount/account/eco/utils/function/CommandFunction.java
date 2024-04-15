package org.kaiaccount.account.eco.utils.function;

import org.mose.command.context.ArgumentContext;
import org.mose.command.context.CommandContext;
import org.mose.command.exception.ArgumentException;

public interface CommandFunction<With, Ret> {

    Ret apply(CommandContext context, ArgumentContext argument, With value) throws ArgumentException;


}
