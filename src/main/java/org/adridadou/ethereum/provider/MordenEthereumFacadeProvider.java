package org.adridadou.ethereum.provider;

import com.typesafe.config.ConfigFactory;
import org.adridadou.ethereum.*;
import org.adridadou.ethereum.keystore.FileSecureKey;
import org.adridadou.ethereum.keystore.SecureKey;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.EthereumFactory;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.List;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class MordenEthereumFacadeProvider implements EthereumFacadeProvider {

    private static class TestNetConfig {
        private final String mordenConfig =
                "peer.discovery = {\n" +
                        "\n" +
                        "    # List of the peers to start\n" +
                        "    # the search of the online peers\n" +
                        "    # values: [ip:port, ip:port, ip:port ...]\n" +
                        "    ip.list = [\n" +
                        "        \"94.242.229.4:40404\",\n" +
                        "        \"94.242.229.203:30303\"\n" +
                        "    ]\n" +
                        "}\n" +
                        "\n" +
                        "# Network id\n" +
                        "peer.networkId = 2\n" +
                        "\n" +
                        "# Enable EIP-8\n" +
                        "peer.p2p.eip8 = true\n" +
                        "\n" +
                        "# the folder resources/genesis\n" +
                        "# contains several versions of\n" +
                        "# genesis configuration according\n" +
                        "# to the network the peer will run on\n" +
                        "genesis = frontier-morden.json\n" +
                        "\n" +
                        "# Blockchain settings (constants and algorithms) which are\n" +
                        "# not described in the genesis file (like MINIMUM_DIFFICULTY or Mining algorithm)\n" +
                        "blockchain.config.name = \"morden\"\n" +
                        "\n" +
                        "database {\n" +
                        "    # place to save physical storage files\n" +
                        "    dir = database-morden\n" +
                        "}\n";


        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(mordenConfig.replaceAll("'", "\"")));
            return props;
        }
    }

    @Override
    public EthereumFacade create() {
        Ethereum ethereum = EthereumFactory.createEthereum(TestNetConfig.class);
        EthereumListenerImpl ethereumListener = new EthereumListenerImpl(ethereum);
        ethereum.init();

        BlockchainProxy proxy = new BlockchainProxyImpl(ethereum, ethereumListener);
        return new EthereumFacade(new EthereumContractInvocationHandler(proxy), proxy, this);
    }

    @Override
    public SecureKey getKey(final String id) throws Exception {
        String homeDir = System.getProperty("user.home");
        return new FileSecureKey(new File(homeDir + "/Library/Ethereum/testnet/keystore/" + id));
    }

    private String getKeystoreFolderPath() {
        String homeDir = System.getProperty("user.home");
        return homeDir + "/Library/Ethereum/testnet/keystore/";
    }

    @Override
    public List<FileSecureKey> listAvailableKeys() {
        return javaslang.collection.List
                .of(new File(getKeystoreFolderPath()).listFiles())
                .filter(File::isFile)
                .map(FileSecureKey::new)
                .toJavaList();
    }
}
