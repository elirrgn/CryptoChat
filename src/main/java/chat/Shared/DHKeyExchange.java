package chat.Shared;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

public class DHKeyExchange {
    private static BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
    + "29024E088A67CC74020BBEA63B139B22514A08798E3404D"
    + "DEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C2"
    + "45E485B576625E7EC6F44C42E9A63A36210000000000090F", 16);;
    private static BigInteger g = BigInteger.valueOf(2);

    public static BigInteger clientSideSharedKeyCreation(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        BigInteger myPrivateKey = new BigInteger(256, new SecureRandom());
        BigInteger myPublicKey = g.modPow(myPrivateKey, p);
        
        BigInteger otherPublic = (BigInteger) in.readObject();
        
        out.writeObject(myPublicKey);
        out.flush();
        
        BigInteger sharedKey = otherPublic.modPow(myPrivateKey, p);
    
        return sharedKey;
    }

    public static BigInteger serverSideSharedKeyCreation(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        BigInteger myPrivateKey = new BigInteger(256, new SecureRandom());
        BigInteger myPublicKey = g.modPow(myPrivateKey, p);
        
        out.writeObject(myPublicKey);
        out.flush();

        BigInteger otherPublic = (BigInteger) in.readObject();

        BigInteger sharedKey = otherPublic.modPow(myPrivateKey, p);
    
        return sharedKey;
    }
}
