package com.nhnextsoft.qrcode.service;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by haipn on 28/05/2022.
 */
public class BlockchainService {
    Web3j web3;
    String buyItemContract = "0x3b39bb8de8276e8c7c1cdc6773b612abf5f3223f";
    public BlockchainService() {
        web3 = Web3j.build(new HttpService("https://data-seed-prebsc-2-s2.binance.org:8545/"));
    }


    public Double getBalance(String address) {
        try {
            BigInteger x =  web3.ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).send().getBalance();
            return convertToEther(x).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public Credentials getWallet(String privateKey) {
        return Credentials.create(privateKey);
    }

    public EthSendTransaction buyItem(Integer id, Integer amount, Double  price,Credentials wallet ) {
        try {
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(wallet.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasLimit = BigInteger.valueOf(200000); // you should get this from api
            BigInteger gasPrice = web3.ethGasPrice().send().getGasPrice();
            BigInteger priceData = Convert.toWei(price.toString(), Convert.Unit.ETHER).toBigInteger();
            List data = Arrays.asList(new Uint(BigInteger.valueOf(id.longValue())), new Uint(BigInteger.valueOf(amount.longValue())));
            Function function = funcTrans("buyItem", data);
            String encodedFunction = FunctionEncoder.encode(function);

            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, buyItemContract, priceData,encodedFunction);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, wallet);
            String hexValue = Numeric.toHexString(signedMessage);

            // Send transaction
            return web3.ethSendRawTransaction(hexValue).sendAsync().get();
        } catch (Exception e) {
            System.out.println("ERROR buy box " + e.getMessage());
            return null;
        }
    }

    private Function funcTrans(String name, List<Type> data) {
        return new Function(
                name,
                data,
                Collections.singletonList(new TypeReference<Bool>() {
                }));
    }

    public BigDecimal convertToEther(BigInteger gwei) {
        return Convert.fromWei(BigDecimal.valueOf(gwei.longValue()), Convert.Unit.ETHER);
    }

//    public static void main(String[] args) {
//        BlockchainService s = new BlockchainService();
//        System.out.println(s.getBalance("0xDBd6Eac4B36f131f3D50c8A8C71cC70D579d0378"));
//        EthSendTransaction x = s.buyItem(1,10,0.0001,s.getWallet("8c3149dc82bd0725b011909a898962ff332a2d88e8bf8c2358fd174962f44678"));
//        System.out.println(x.getTransactionHash());
//    }
}
