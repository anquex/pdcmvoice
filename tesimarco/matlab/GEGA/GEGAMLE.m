clc;
clear all;
close all;

if(matlabpool('size') == 0)
    maxNumCompThreads(2);
    matlabpool local 2;
end;

T=30;
slots=20;
GMLE=zeros(T+1,T+1,slots);
b=2;
p=1./(b.^(1:60));

parfor l=1:slots
    l
    tic;
    sfG0=sfG(l);
    
    sficsT0='((1-p(l))^n)^i * (n*p(l)*(1-p(l))^(n-1))^s * (1-(1-p(l))^n-n*p(l)*(1-p(l))^(n-1))^c';
    sfG1=diff(sfG0,'n');
    sficsT1=diff(sficsT0,'n');
    sfG1=char(sfG1);
    sficsT1=char(sficsT1);
    sfT0=[sficsT0 ' * '  sfG0];
    sfT0=strrep(sfT0,'*','.*');
    sfT0=strrep(sfT0,'^','.^');
    sfT0=strrep(sfT0,'/','./');
    fT0=inline(sfT0,'p','n','l','i','s','c');
    GMLEtmp=zeros(T+1,T+1);
    for c=0:T
        for s=0:T-c
            i=T-s-c;
            GMLEtmp(c+1,s+1)=deasysolve(fT0,p,l,i,s,c);
        end
    end
    GMLE(:,:,l)=GMLEtmp;
    toc;
end

save(['GMLE-T-' int2str(T)], 'GMLE','T','b','slots','p');