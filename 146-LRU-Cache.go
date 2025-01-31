type LRUCache struct {
    Capacity int
    Cache map[int]*list.Element
    Queue *list.List
}

type LRUNode struct{
    Key, Value int
}


func Constructor(capacity int) LRUCache {
    return LRUCache{capacity, make(map[int]*list.Element), list.New()}
}


func (this *LRUCache) Get(key int) int {
    if node, ok := this.Cache[key]; ok{
        val := node.Value.(*LRUNode).Value
        this.Queue.MoveToFront(node)
        return val
    }
    return -1
}


func (this *LRUCache) Put(key int, value int)  {
    if node, ok := this.Cache[key]; ok{
        this.Queue.MoveToFront(node)
        node.Value.(*LRUNode).Value = value 
        return
    } else {
        newNode := &LRUNode{key,value}
        nodeRef := this.Queue.PushFront(newNode)
        this.Cache[key] = nodeRef
    }
    if len(this.Cache) > this.Capacity{
        LRU := this.Queue.Back()
        key := LRU.Value.(*LRUNode).Key
        this.Queue.Remove(LRU)
        delete(this.Cache, key)
    }
    
}


/**
 * Your LRUCache object will be instantiated and called as such:
 * obj := Constructor(capacity);
 * param_1 := obj.Get(key);
 * obj.Put(key,value);
 */