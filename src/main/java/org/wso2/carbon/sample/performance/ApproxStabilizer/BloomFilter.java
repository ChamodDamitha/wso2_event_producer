package org.wso2.carbon.sample.performance.ApproxStabilizer;/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 * A probabilistic data structure to check the membership of items in a set
 *
 * @param <E> is the type of data to be stored
 */
public class BloomFilter<E> {
    //  bit array to keep track of hashed values
    private BitSet bitSet;
    private int sizeOfBitSet;
    private int expectNoOfElements;
    private int insertedNoOfElements;
    private int noOfHashFunctions;

    private MessageDigest messageDigest;

    /**
     * @param sizeOfBitSet       is the size of bit array
     * @param expectNoOfElements is the expected number of insertions
     * @param noOfHashFunctions  is the number of hash functions to be used
     */
    private BloomFilter(int sizeOfBitSet, int expectNoOfElements, int noOfHashFunctions) {
        this.expectNoOfElements = expectNoOfElements;
        this.noOfHashFunctions = noOfHashFunctions;
        this.sizeOfBitSet = sizeOfBitSet;
        this.bitSet = new BitSet(sizeOfBitSet);

//        setting MD5 digest function to generate hashes
        try {
            this.messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error while ....'" + "' in Siddhi app , " + e.getMessage());
        }


//        System.out.println("sizeOfBitSet : " + sizeOfBitSet); //TODO: added only for testing
//        System.out.println("noOfHashFunctions : " + noOfHashFunctions); //TODO: added only for testing
    }

    /**
     * Create a bloom filter by specifying the expected number of elements and the false positive probability
     *
     * @param expectNoOfElements       is the expected number of insertions
     * @param falsePositiveProbability is the false probability of the bloom filter
     */
    public static BloomFilter createBloomFilter(int expectNoOfElements, double falsePositiveProbability) {
//      m = -nln(p)/(ln(2))^2
        int sizeOfBitSet = -(int) Math.ceil(expectNoOfElements * Math.log(falsePositiveProbability)
                / Math.pow(Math.log(2), 2));
//      k = mln(2)/n
        int noOfHashFunctions = (int) Math.ceil(sizeOfBitSet * Math.log(2) / expectNoOfElements);

        return new BloomFilter(sizeOfBitSet, expectNoOfElements, noOfHashFunctions);
    }

    /**
     * Compute a cell position in the bit array for a given hash value
     *
     * @param hash is the integer hash value generated from some hash function
     * @return
     */
    private int getBitIndex(int hash) {
        return Math.abs(hash % sizeOfBitSet);
    }


    /**
     * Compute a set of different int values for a byte array of data
     *
     * @param data       is the array of bytes for which the hash values are needed
     * @param noOfHashes is the number of hash values needed
     * @return an int array of hash values
     */
    private int[] getHashValues(byte[] data, int noOfHashes) {
        int[] hashValues = new int[noOfHashes];
        byte salt = 0;
        int completedHashes = 0;
        byte[] digest;
        int hash;

//      Loop until the number of hash values are completed
        while (completedHashes < noOfHashes) {
            messageDigest.update(salt);
            digest = messageDigest.digest(data);

//            System.out.println("digest " + digest.toString()); //TODO: added only for testing

//          jump from 4 by 4 to create some randomness
            for (int i = 0; i < digest.length / 4; i++) {
                hash = 1;
                for (int j = 4 * i; j < (4 * i + 4); j++) {
//                  multiply the hash by 2^8
                    hash <<= 8;
//                  the least significant byte of digest[j] is taken
                    hash |= ((int) digest[j]) & 0xff;


                    hashValues[completedHashes] = hash;
                    completedHashes++;

                    if (completedHashes == noOfHashes) {
                        return hashValues;
                    }
                }
            }

//          salt is incremented to obtain new values for the digest
            salt++;

        }

        return hashValues;
    }

    /**
     * return a byte array for input data of type E
     *
     * @param data
     * @return a byte array
     */
    private byte[] getBytes(E data) {
        return data.toString().getBytes();
    }

    /**
     * Adds a value to the bloom filter
     *
     * @param item
     */
    public void insert(E item) {
        byte[] dataBytes = getBytes(item);
        int[] bitIndices = getHashValues(dataBytes, this.noOfHashFunctions);

//      set the relevant bits in the array to 1
        for (int i : bitIndices) {
            bitSet.set(getBitIndex(i));

//            System.out.println("bit index " + getBitIndex(i)); //TODO : added only for testing
        }

        insertedNoOfElements++;
    }

    /**
     * Check whether the queried data is contained within the bloom filter
     *
     * @param data is the data to be checked for the membership
     * @return {@code true} if the data may contain, {@code false} if the data does not contain
     */
    public boolean mayContain(E data) {
        byte[] dataBytes = getBytes(data);
        int[] bitIndices = getHashValues(dataBytes, this.noOfHashFunctions);

        for (int i : bitIndices) {
//          if at least one bit is set to 0, return 0
            if (!bitSet.get(getBitIndex(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * calculate the false positive probability based on the expected number of elements inserted
     *
     * @return the probability of false positives
     */
    public double getFalsePositiveProbability() {
//        p = (1 - e ^ (-k * n / m)) ^ k
        return Math.pow(1 - Math.exp(-noOfHashFunctions * expectNoOfElements / (double) sizeOfBitSet), noOfHashFunctions);
    }

    /**
     * Return the inserted number of elements to the bloom filter
     *
     * @return
     */
    public int getInsertedNoOfElements() {
        return this.insertedNoOfElements;
    }

    public void clear() {
        this.bitSet = new BitSet(sizeOfBitSet);
    }
}
