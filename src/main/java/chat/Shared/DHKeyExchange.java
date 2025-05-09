package chat.Shared;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;


/**
 * Utility class to manage the Dieffie Hellman key exchange algorithm
 */
public class DHKeyExchange {
    private static final Logger logger = LogManager.getLogger(DHKeyExchange.class);
    static {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.DEBUG);
    }

    private static BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
    + "29024E088A67CC74020BBEA63B139B22514A08798E3404D"
    + "DEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C2"
    + "45E485B576625E7EC6F44C42E9A63A36210000000000090F", 16);;// a fixed number doesn't impact the security
    private static BigInteger g = BigInteger.valueOf(2);


    /**
     * Generates a shared secret key on the client side using the Diffie-Hellman key exchange protocol.
     * 
     * @param out the ObjectOutputStream used to send the client's public key to the server
     * @param in the ObjectInputStream used to receive the server's public key
     * @return the computed shared secret BigInteger
     * @throws Exception if an I/O error occurs during communication or if an unexpected object is read
     */
    public static BigInteger clientSideSharedKeyCreation(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        logger.debug("Client generating private and public keys");
        BigInteger myPrivateKey = new BigInteger(256, new SecureRandom());
        BigInteger myPublicKey = g.modPow(myPrivateKey, p);
        
        BigInteger otherPublic = (BigInteger) in.readObject();
        
        out.writeObject(myPublicKey);
        out.flush();
        
        BigInteger sharedKey = otherPublic.modPow(myPrivateKey, p);
    
        logger.debug("Client created shared key.");
        return sharedKey;
    }

    /**
     * Generates a shared secret key on the server side using the Diffie-Hellman key exchange protocol.
     * 
     * @param out the ObjectOutputStream used to send the server's public key to the client
     * @param in the ObjectInputStream used to receive the client's public key
     * @return the computed shared secret BigInteger
     * @throws Exception if an I/O error occurs during communication or if an unexpected object is read
     */
    public static BigInteger serverSideSharedKeyCreation(ObjectOutputStream out, ObjectInputStream in) throws Exception {
        logger.debug("Server generating private and public keys");
        BigInteger myPrivateKey = new BigInteger(256, new SecureRandom());
        BigInteger myPublicKey = g.modPow(myPrivateKey, p);
        
        out.writeObject(myPublicKey);
        out.flush();

        BigInteger otherPublic = (BigInteger) in.readObject();

        BigInteger sharedKey = otherPublic.modPow(myPrivateKey, p);
    
        logger.debug("Server created shared key.");
        return sharedKey;
    }
}
