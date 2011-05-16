#include <stdio.h>
#include <stdlib.h>

struct list_el {
	int val;
	struct list_el* next;
};
typedef struct list_el list_el;

void insert(list_el** head, int value);
void printList(list_el* head);
void append(list_el** head, int value);
int find(list_el* head, int value);
int length(list_el* head);

int main()
{
	list_el* list = NULL;
	int i;
	for(i = 0; i < 100; i+=2)
	{
		append(&list, i);
	}
	for(i = 1; i < 100; i+=2)
	{
		insert(&list,i);
	}
	
	//printf("%d\n",length(list));
	//printf("%d\n",find(list, 100));
	//printList(list);

	return 0;
}

//-----------------
//Insert takes:
//	head - pointer to a pointer the head of the list
//	value - value to be inserted
//
//Inserts value at the correct position (asc) in the list
//-----------------
void insert(list_el** head, int value)
{
	list_el* new_el = (list_el*)malloc(sizeof(list_el));
	new_el->val = value;

	//if the list is empty, set next to null and return
	if(*head == NULL)
	{
		new_el->next = NULL;
		*head = new_el;
	}
	else
	{
	
		list_el* it = *head;
		
		//find either last element or last element in list whose
		//value is less than that of the new element
		while(it->next != NULL && new_el->val > it->next->val)
		{
			it = it->next;
		}
		
		//position new element between it and it->next
		if(new_el->val > it->val)
		{
			list_el* temp = it->next;
			it->next = new_el;
			new_el->next = temp;
		}
		//position new element before iterator
		else
		{
			new_el->next = it;
			*head = new_el;
		}
	
	}
}

//-----------------
//Append takes:
//	head - a pointer to a pointer to the head of the list
//	value - the value to be appended
//
//Appends a given value to the end of the list
//-----------------
void append(list_el** head, int value)
{
	list_el* new_el = (list_el*)malloc(sizeof(list_el));
	new_el->val=value;
	
	list_el* it = *head;
	
	//if list is empty
	if(it == NULL)
		*head = new_el;
	else
	{
		//get iterator to end of the list
		while(it->next != NULL)
		{
			it = it->next;
		}
		it->next = new_el;
	}
	
}
//-----------------
//printList takes:
//	head-a pointer to the head of the list
//	
//Prints head's values to stdout
//-----------------
void printList(list_el* head)
{
	list_el* it = head;
	while(it != NULL)
	{
		printf("%d ",it->val);
		it = it->next;
	}
}

//-----------------
//find takes:
//	head - a list to be searched
//	value - the value to be searched for
//
//returns:
//	index if found
//	-1 if not found
//
//Performs a sequential search on the list (O(n))
//-----------------
int find(list_el* head, int value)
{
	int i = 0;
	while(head != NULL)
	{
		if(head->val == value)
			return i;
		else
		{
			i++;
			head=head->next;
		}
	}
	return -1;
}

//-----------------
//Length takes:
//	head - pointer to list who's length is returned
//
//Returns the length of the list in O(n) time
//-----------------
int length(list_el* head)
{
	int i = 0;
	while(head != NULL)
	{
		head = head->next;
		i++;
	}
	return i;

}