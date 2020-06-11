/*
 * Copyright 2004-2020 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (https://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.expression.function;

import org.h2.engine.Session;
import org.h2.expression.Expression;
import org.h2.expression.OperationN;
import org.h2.expression.TypedValueExpression;
import org.h2.message.DbException;
import org.h2.security.BlockCipher;
import org.h2.security.CipherFactory;
import org.h2.util.MathUtils;
import org.h2.util.Utils;
import org.h2.value.DataType;
import org.h2.value.TypeInfo;
import org.h2.value.Value;
import org.h2.value.ValueNull;
import org.h2.value.ValueVarbinary;

/**
 * An ENCRYPT or DECRYPT function.
 */
public class CryptFunction extends OperationN implements NamedExpression {

    /**
     * ENCRYPT() (non-standard).
     */
    public static final int ENCRYPT = 0;

    /**
     * DECRYPT() (non-standard).
     */
    public static final int DECRYPT = ENCRYPT + 1;

    private static final String[] NAMES = { //
            "ENCRYPT", "DECRYPT" //
    };

    private final int function;

    public CryptFunction(Expression arg1, Expression arg2, Expression arg3, int function) {
        super(new Expression[] { arg1, arg2, arg3 });
        this.function = function;
    }

    @Override
    public Value getValue(Session session) {
        Value v1 = args[0].getValue(session);
        if (v1 == ValueNull.INSTANCE) {
            return ValueNull.INSTANCE;
        }
        Value v2 = args[1].getValue(session);
        if (v2 == ValueNull.INSTANCE) {
            return ValueNull.INSTANCE;
        }
        Value v3 = args[2].getValue(session);
        if (v3 == ValueNull.INSTANCE) {
            return ValueNull.INSTANCE;
        }
        BlockCipher cipher = CipherFactory.getBlockCipher(v1.getString());
        cipher.setKey(getPaddedArrayCopy(v2.getBytesNoCopy(), cipher.getKeyLength()));
        byte[] newData = getPaddedArrayCopy(v3.getBytesNoCopy(), BlockCipher.ALIGN);
        switch (function) {
        case ENCRYPT: {
            cipher.encrypt(newData, 0, newData.length);
            break;
        }
        case DECRYPT:
            cipher.decrypt(newData, 0, newData.length);
            break;
        default:
            throw DbException.throwInternalError("function=" + function);
        }
        return ValueVarbinary.getNoCopy(newData);
    }

    private static byte[] getPaddedArrayCopy(byte[] data, int blockSize) {
        return Utils.copyBytes(data, MathUtils.roundUpInt(data.length, blockSize));
    }

    @Override
    public Expression optimize(Session session) {
        boolean allConst = optimizeArguments(session, true);
        TypeInfo t = args[2].getType();
        type = DataType.isBinaryStringType(t.getValueType())
                ? TypeInfo.getTypeInfo(Value.VARBINARY, t.getPrecision(), 0, null)
                : TypeInfo.TYPE_VARBINARY;
        if (allConst) {
            return TypedValueExpression.getTypedIfNull(getValue(session), type);
        }
        return this;
    }

    @Override
    public StringBuilder getUnenclosedSQL(StringBuilder builder, int sqlFlags) {
        return writeExpressions(builder.append(getName()).append('('), args, sqlFlags).append(')');
    }

    @Override
    public String getName() {
        return NAMES[function];
    }

}
