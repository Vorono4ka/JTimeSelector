package com.vorono4ka;

import java.util.Arrays;

public class LongList {
    public static final int DEFAULT_SIZE = 8;

    private long[] array;
    private int size;

    /**
     * {@code size} defaults to {@link #DEFAULT_SIZE}
     * @see #LongList(int) LongList
     */
    public LongList() {
        this(DEFAULT_SIZE);
    }

    public LongList(int size) {
        this.array = new long[size];
    }

    public LongList(long[] array) {
        this.array = array;
        this.size = array.length;
    }

    public long get(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException(index);
        }

        return array[index];
    }

    public void set(int index, long value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException(index);
        }

        array[index] = value;
    }

    public void add(long value) {
        if (size >= array.length) {
            array = Arrays.copyOf(array, size + DEFAULT_SIZE);
        }
        array[size++] = value;
    }

    public void addAll(LongList list) {
        if (list.size > 0) {
            int newSize = size + list.size;
            if (newSize > array.length) {
                array = Arrays.copyOf(array, newSize);
            }
            System.arraycopy(list.array, 0, array, size, list.size);
            size = newSize;
        }
    }

    public void remove(int index) {
        size--;
        for (int i = index; i < size; i++) {
            array[i] = array[i + 1];
        }
    }

    /**
     * Removes first found element.
     *
     * @param value value to remove
     */
    public void removeValue(long value) {
        for (int i = 0; i < size; i++) {
            if (array[i] == value) {
                remove(i);
                break;
            }
        }
    }

    /**
     * @param value searching value
     * @return element index if exists, otherwise -1
     */
    public int indexOf(long value) {
        for (int i = 0; i < size; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    public boolean contains(int value) {
        return indexOf(value) != -1;
    }

    /**
     * Trims elements array to elements count.
     */
    public void trim() {
        array = Arrays.copyOf(array, size);
    }

    /**
     * {@code trimToSize} defaults to {@code true}
     * @return list values as array
     * @see #toArray(boolean)
     */
    public long[] toArray() {
        return toArray(true);
    }

    /**
     * @param trimToSize should trim array to elements count
     * @return list values as array
     */
    public long[] toArray(boolean trimToSize) {
        return Arrays.copyOf(this.array, trimToSize ? this.size : this.array.length);
    }

    /**
     * @return list size
     */
    public int size() {
        return size;
    }

    /**
     * Avoid to use this method.
     * @return array object
     */
    public long[] getArray() {
        return array;
    }

    /**
     * Sets list size to zero.
     */
    public void clear() {
        size = 0;
    }
}
