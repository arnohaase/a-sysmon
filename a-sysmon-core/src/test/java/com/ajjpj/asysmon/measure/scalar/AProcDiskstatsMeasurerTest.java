package com.ajjpj.asysmon.measure.scalar;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author arno
 */
public class AProcDiskstatsMeasurerTest {
    static final String DISKSTATS_1 =
        "   1       0 ram0 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1       1 ram1 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1       2 ram2 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1       3 ram3 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1       4 ram4 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1       5 ram5 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1       6 ram6 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1       7 ram7 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1       8 ram8 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1       9 ram9 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1      10 ram10 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1      11 ram11 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1      12 ram12 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1      13 ram13 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1      14 ram14 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   1      15 ram15 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   7       0 loop0 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   7       1 loop1 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   7       2 loop2 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   7       3 loop3 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   7       4 loop4 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   7       5 loop5 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   7       6 loop6 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   7       7 loop7 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   8       0 sda 52938 13190 2482424 71876 9900 8726 3117160 71552 1 16984 143440\n" +
        "   8       1 sda1 24401 3642 1390166 56080 2329 1392 2025488 25704 0 6512 81780\n" +
        "   8       2 sda2 163 369 1548 164 0 0 0 0 0 164 164\n" +
        "   8       3 sda3 2 0 4 0 0 0 0 0 0 0 0\n" +
        "   8       5 sda5 28194 9179 1089282 15404 7556 7334 1091672 45776 0 12204 61196\n" +
        "  11       0 sr0 0 0 0 0 0 0 0 0 0 0 0\n" +
        "   8      16 sdb 0 0 0 0 0 0 0 0 0 0 0\n";

    static final String DISKSTATS_2 =
            "1 0 ram0 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 1 ram1 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 2 ram2 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 3 ram3 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 4 ram4 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 5 ram5 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 6 ram6 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 7 ram7 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 8 ram8 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 9 ram9 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 10 ram10 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 11 ram11 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 12 ram12 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 13 ram13 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 14 ram14 0 0 0 0 0 0 0 0 0 0 0\n" +
            "1 15 ram15 0 0 0 0 0 0 0 0 0 0 0\n" +
            "7 0 loop0 0 0 0 0 0 0 0 0 0 0 0\n" +
            "7 1 loop1 0 0 0 0 0 0 0 0 0 0 0\n" +
            "7 2 loop2 0 0 0 0 0 0 0 0 0 0 0\n" +
            "7 3 loop3 0 0 0 0 0 0 0 0 0 0 0\n" +
            "7 4 loop4 0 0 0 0 0 0 0 0 0 0 0\n" +
            "7 5 loop5 0 0 0 0 0 0 0 0 0 0 0\n" +
            "7 6 loop6 0 0 0 0 0 0 0 0 0 0 0\n" +
            "7 7 loop7 0 0 0 0 0 0 0 0 0 0 0\n" +
            "8 0 sda 6140782 2758188 423107196 13161140 12925861 53306861 529847732 11057850 0 14935249 24212400\n" +
            "8 1 sda1 43272 7861 5796576 71092 1357 1246 5212 2169 0 47272 73240\n" +
            "8 2 sda2 6096799 2749911 417301604 13086879 12924504 53305615 529842520 11055681 0 14885724 24135992\n" +
            "8 16 sdb 3339663 70223 367851671 6718867 85176401 170504258 2045450224 33782894 0 15531377 40445476\n" +
            "8 17 sdb1 3338949 69713 367841879 6716536 85176401 170504258 2045450224 33782894 0 15529268 40443145\n" +
            "11 0 sr0 0 0 0 0 0 0 0 0 0 0 0\n" +
            "253 0 dm-0 870965 0 42049802 2132261 372915 0 2983320 379723 3 1532381 2512012\n" +
            "253 1 dm-1 3103356 0 24826848 3149251 4506322 0 36050576 13538748 0 603613 16688227\n" +
            "253 2 dm-2 419777 0 9246850 1130505 292947 0 2343576 3833502 0 889303 4964026\n" +
            "253 3 dm-3 2191075 0 287285818 3289814 7804713 0 62437704 9322738 0 3150483 12612543\n" +
            "253 4 dm-4 789111 0 71292018 2558169 247583618 0 1980668944 1300063521 0 11586560 1302621928\n" +
            "253 5 dm-5 3244560 0 129918562 6801646 740100 0 5920800 1734083 0 5467910 8535868\n" +
            "253 6 dm-6 463275 0 31126818 1363898 5502758 0 44022064 3245804 0 2067703 4609716\n" +
            "253 7 dm-7 498205 0 72771082 1213301 4236021 0 33888168 954063 0 1119057 2167415\n" +
            "253 8 dm-8 573912 0 100651274 1219099 246759 0 1974072 155954 0 904552 1375133\n" +
            "253 9 dm-9 87402 0 15903346 178025 50625440 0 405003520 171947556 0 3335575 172128426";

    @Test
    public void testSnapshot1() throws Exception {
        final AProcDiskstatsMeasurer.Snapshot snapshot = AProcDiskstatsMeasurer.createSnapshot(Arrays.asList(DISKSTATS_1.split("\n")));
        assertEquals(31, snapshot.iosInProgress.size());

        assertEquals(2482424, (long) snapshot.sectorsRead.get("sda"));
        assertEquals(1390166, (long) snapshot.sectorsRead.get("sda1"));

        assertEquals(3117160, (long) snapshot.sectorsWritten.get("sda"));
        assertEquals(2025488, (long) snapshot.sectorsWritten.get("sda1"));

        assertEquals(1, (long) snapshot.iosInProgress.get("sda"));
        assertEquals(0, (long) snapshot.iosInProgress.get("sda1"));
    }

    @Test
    public void testSnapshot2() throws Exception {
        final AProcDiskstatsMeasurer.Snapshot snapshot = AProcDiskstatsMeasurer.createSnapshot(Arrays.asList(DISKSTATS_2.split("\n")));
        assertEquals(40, snapshot.iosInProgress.size());

        assertEquals(423107196, (long) snapshot.sectorsRead.get("sda"));
        assertEquals(42049802, (long) snapshot.sectorsRead.get("dm-0"));

        assertEquals(529847732, (long) snapshot.sectorsWritten.get("sda"));
        assertEquals(2983320, (long) snapshot.sectorsWritten.get("dm-0"));

        assertEquals(0, (long) snapshot.iosInProgress.get("sda"));
        assertEquals(3, (long) snapshot.iosInProgress.get("dm-0"));
    }
}
