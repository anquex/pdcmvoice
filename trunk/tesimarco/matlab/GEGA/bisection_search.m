function i=bisection_search(a,x)

%  i=bisection_search(a,x)
%  a= array ordinato asc
%  x valore da trovare
%  i indice del più grande elemento minore di x
%    0 se tutti maggiori
%    length(a) se tutti minori
%    i+1 è la posizione nell'array in cui fare l'inserimento

l=1;
r=length(a);
while (l<=r)
    i=floor((l+r)/2);
    if (x>a(i))
        l=i+1;
    else
        r=i-1;
    end
end
i=floor((l+r)/2);
