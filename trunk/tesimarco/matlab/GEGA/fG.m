% calcola la propabilità di finire nello slot l per ciascuna taglia dato un vettore n di taglie
function y=fG(p,n,l)
    if(length(p)<l)
        error('vettore p troppo corto');
    end
    sfG0=sfG(l);
    sfG0=strrep(sfG0,'*','.*');
    sfG0=strrep(sfG0,'^','.^');
    sfG0=strrep(sfG0,'/','./');
    f=inline(sfG0,'p','n','l');
    y=f(p,n,l);
    